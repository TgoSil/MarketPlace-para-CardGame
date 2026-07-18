package com.quatro.matching_service.domain.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketStateService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // --- AUCTION INFO ---
    public void saveAuctionInfo(AuctionInfo info) {
        try {
            String json = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set("auction:" + info.idAuction() + ":info", json);
            // Adiciona a um índice de "Auctions ativas" daquela carta
            redisTemplate.opsForSet().add("card:" + info.idCarta() + ":auctions", info.idAuction().toString());
            // Adiciona num set global de "Auctions ativas" para checar expiração
            redisTemplate.opsForSet().add("auctions:active", info.idAuction().toString());
        } catch (JsonProcessingException e) {
            log.error("Erro ao salvar AuctionInfo", e);
        }
    }

    public AuctionInfo getAuctionInfo(UUID auctionId) {
        String json = redisTemplate.opsForValue().get("auction:" + auctionId + ":info");
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, AuctionInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Erro ao ler AuctionInfo", e);
            return null;
        }
    }

    public void removeAuction(UUID auctionId, UUID idCarta) {
        redisTemplate.delete("auction:" + auctionId + ":info");
        redisTemplate.delete("auction:" + auctionId + ":bids");
        redisTemplate.opsForSet().remove("card:" + idCarta + ":auctions", auctionId.toString());
        redisTemplate.opsForSet().remove("auctions:active", auctionId.toString());
    }

    public Set<UUID> getActiveAuctions() {
        Set<String> auctions = redisTemplate.opsForSet().members("auctions:active");
        if (auctions == null) return Set.of();
        return auctions.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    // --- BID INFO ---
    public void saveBidInfo(BidInfo info) {
        try {
            String json = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set("bid:" + info.idBid() + ":info", json);
            redisTemplate.opsForSet().add("bids:active", info.idBid().toString());
        } catch (JsonProcessingException e) {
            log.error("Erro ao salvar BidInfo", e);
        }
    }

    public BidInfo getBidInfo(UUID bidId) {
        String json = redisTemplate.opsForValue().get("bid:" + bidId + ":info");
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, BidInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Erro ao ler BidInfo", e);
            return null;
        }
    }

    public void removeBid(UUID bidId) {
        redisTemplate.delete("bid:" + bidId + ":info");
        redisTemplate.opsForSet().remove("bids:active", bidId.toString());
    }

    public Set<UUID> getActiveBids() {
        Set<String> bids = redisTemplate.opsForSet().members("bids:active");
        if (bids == null) return Set.of();
        return bids.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    // --- FILAS DE PRIORIDADE (ZSET) ---
    public void updateBidInAuction(UUID auctionId, UUID bidId, BigDecimal currentBidValue) {
        redisTemplate.opsForZSet().add("auction:" + auctionId + ":bids", bidId.toString(), currentBidValue.doubleValue());
    }

    public void removeBidFromAuction(UUID auctionId, UUID bidId) {
        redisTemplate.opsForZSet().remove("auction:" + auctionId + ":bids", bidId.toString());
    }

    public UUID getTopBidder(UUID auctionId) {
        Set<String> top = redisTemplate.opsForZSet().reverseRange("auction:" + auctionId + ":bids", 0, 0);
        if (top == null || top.isEmpty()) return null;
        return UUID.fromString(top.iterator().next());
    }

    public BigDecimal getTopBidValue(UUID auctionId) {
        Set<String> top = redisTemplate.opsForZSet().reverseRange("auction:" + auctionId + ":bids", 0, 0);
        if (top == null || top.isEmpty()) return null;
        Double score = redisTemplate.opsForZSet().score("auction:" + auctionId + ":bids", top.iterator().next());
        return score != null ? BigDecimal.valueOf(score) : null;
    }

    // --- CONSULTAS EXTRAS ---
    public Set<UUID> getAuctionsForCard(UUID cardId) {
        Set<String> auctions = redisTemplate.opsForSet().members("card:" + cardId + ":auctions");
        if (auctions == null) return Set.of();
        return auctions.stream().map(UUID::fromString).collect(Collectors.toSet());
    }
}
