package com.quatro.matching_service.service;

import com.quatro.matching_service.domain.event.IntencaoCanceladaEvent;
import com.quatro.matching_service.domain.event.LeilaoConcluidoEvent;
import com.quatro.matching_service.domain.redis.AuctionInfo;
import com.quatro.matching_service.domain.redis.BidInfo;
import com.quatro.matching_service.domain.redis.MarketStateService;
import com.quatro.matching_service.kafka.MatchingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketWorker {

    private final MarketStateService marketStateService;
    private final MatchingEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final RoutingService routingService;

    // Executa o Tick a cada 2 segundos
    @Scheduled(fixedRate = 2000)
    public void runMarketTick() {
        faseAPensamentoRobos();
        faseBExecucaoLeiloes();
    }

    private void faseAPensamentoRobos() {
        Set<UUID> bidsAtivas = marketStateService.getActiveBids();
        for (UUID bidId : bidsAtivas) {
            BidInfo bid = marketStateService.getBidInfo(bidId);
            if (bid == null) continue;

            // Verifica se o Bid expirou
            if (bid.expiraEm() != null && Instant.now().isAfter(bid.expiraEm())) {
                log.info("Bid {} expirou. Cancelando intenção...", bidId);
                // Remove das filas de leilão
                for (UUID auctionId : bid.salasAtuais()) {
                    marketStateService.removeBidFromAuction(auctionId, bidId);
                }
                marketStateService.removeBid(bidId);
                eventPublisher.publicarIntencaoCancelada(new IntencaoCanceladaEvent(
                        "BID", bidId, bid.idCarta(), bid.idComprador(), "EXPIRADA"
                ));
                continue;
            }

            // Se não está em nenhuma sala, tenta entrar agora (útil para bids que chegaram antes da auction)
            if (bid.salasAtuais().isEmpty()) {
                routingService.rotearNovoBid(bid);
            }
        }

        // 2. Resolução Instantânea do Proxy Bidding (Por Sala de Leilão)
        Set<UUID> auctionsAtivas = marketStateService.getActiveAuctions();
        for (UUID auctionId : auctionsAtivas) {
            AuctionInfo auction = marketStateService.getAuctionInfo(auctionId);
            if (auction == null) continue;

            Set<String> robotIds = redisTemplate.opsForZSet().range("auction:" + auctionId + ":bids", 0, -1);
            if (robotIds == null || robotIds.isEmpty()) continue;

            BidInfo highestBidder = null;
            BidInfo secondHighestBidder = null;

            for (String bId : robotIds) {
                BidInfo bid = marketStateService.getBidInfo(UUID.fromString(bId));
                if (bid == null) continue;

                BigDecimal limite = bid.limitePagamento() != null ? bid.limitePagamento() : BigDecimal.ZERO;

                if (highestBidder == null) {
                    highestBidder = bid;
                } else {
                    BigDecimal highestLimite = highestBidder.limitePagamento() != null ? highestBidder.limitePagamento() : BigDecimal.ZERO;
                    
                    if (limite.compareTo(highestLimite) > 0) {
                        secondHighestBidder = highestBidder;
                        highestBidder = bid;
                    } else {
                        BigDecimal secondLimite = secondHighestBidder != null && secondHighestBidder.limitePagamento() != null ? secondHighestBidder.limitePagamento() : BigDecimal.ZERO;
                        if (secondHighestBidder == null || limite.compareTo(secondLimite) > 0) {
                            secondHighestBidder = bid;
                        }
                    }
                }
            }

            if (highestBidder != null) {
                BigDecimal minimo = auction.precoMinimo() != null ? auction.precoMinimo() : BigDecimal.ZERO;
                BigDecimal winningPrice = minimo;
                
                if (secondHighestBidder != null) {
                    BigDecimal secondLimite = secondHighestBidder.limitePagamento() != null ? secondHighestBidder.limitePagamento() : BigDecimal.ZERO;
                    winningPrice = secondLimite.add(BigDecimal.ONE);
                    
                    BigDecimal highestLimite = highestBidder.limitePagamento() != null ? highestBidder.limitePagamento() : BigDecimal.ZERO;
                    if (winningPrice.compareTo(highestLimite) > 0) {
                        winningPrice = highestLimite;
                    }
                }

                // Proteção: não deixar o preço cair abaixo do mínimo
                if (winningPrice.compareTo(minimo) < 0) {
                    winningPrice = minimo;
                }

                // Define o preço vencedor real
                marketStateService.updateBidInAuction(auctionId, highestBidder.idBid(), winningPrice);
                log.info("Proxy Bidding na sala {}: Robô {} assumiu a liderança com {}", auctionId, highestBidder.idBid(), winningPrice);
                
                // Zera o score dos perdedores (para garantir que o ZREVRANGE do script Lua pegue o vencedor)
                for (String bId : robotIds) {
                    if (!bId.equals(highestBidder.idBid().toString())) {
                        marketStateService.updateBidInAuction(auctionId, UUID.fromString(bId), BigDecimal.ZERO);
                    }
                }
            }
        }
    }

    private void faseBExecucaoLeiloes() {
        Set<UUID> auctionsAtivas = marketStateService.getActiveAuctions();
        Instant agora = Instant.now();

        for (UUID auctionId : auctionsAtivas) {
            AuctionInfo auction = marketStateService.getAuctionInfo(auctionId);
            if (auction == null) continue;

            if (agora.isAfter(auction.expiraEm())) {
                log.info("Auction {} expirou. Iniciando Lua Script atômico para match...", auctionId);
                executarMatchAtomico(auction);
            }
        }
    }

    private void executarMatchAtomico(AuctionInfo auction) {
        // Lua Script: 
        // 1. Pega o maior score (zrevrange)
        // 2. Apaga a fila e as infos
        // Retorna o vencedor e o valor pago, ou null
        String luaScript = """
            local auctionId = KEYS[1]
            local top = redis.call('ZREVRANGE', 'auction:' .. auctionId .. ':bids', 0, 0, 'WITHSCORES')
            if #top > 0 then
                local winner = top[1]
                local value = top[2]
                -- Limpeza da sala
                redis.call('DEL', 'auction:' .. auctionId .. ':bids')
                redis.call('DEL', 'auction:' .. auctionId .. ':info')
                redis.call('SREM', 'auctions:active', auctionId)
                redis.call('SREM', 'card:' .. ARGV[1] .. ':auctions', auctionId)
                return winner .. ':' .. value
            else
                -- Ninguém deu lance
                redis.call('DEL', 'auction:' .. auctionId .. ':bids')
                redis.call('DEL', 'auction:' .. auctionId .. ':info')
                redis.call('SREM', 'auctions:active', auctionId)
                redis.call('SREM', 'card:' .. ARGV[1] .. ':auctions', auctionId)
                return 'NO_WINNER'
            end
        """;

        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(String.class);

        List<String> keys = Collections.singletonList(auction.idAuction().toString());
        String result = redisTemplate.execute(script, keys, auction.idCarta().toString());

        if ("NO_WINNER".equals(result)) {
            log.info("Auction {} expirou sem lances. Enviando IntencaoCancelada.", auction.idAuction());
            eventPublisher.publicarIntencaoCancelada(new IntencaoCanceladaEvent(
                    "AUCTION",
                    auction.idAuction(),
                    auction.idCarta(),
                    auction.idVendedor(),
                    "EXPIRADA"
            ));
        } else if (result != null) {
            String[] parts = result.split(":");
            UUID winnerId = UUID.fromString(parts[0]);
            BigDecimal valor = new BigDecimal(parts[1]);

            log.info("MATCH! Auction {} vencida pelo Robô {} pagando {}", auction.idAuction(), winnerId, valor);
            
            // Pega infos do robô vencedor (Double Spend logic: remove de outras filas depois)
            BidInfo winnerBid = marketStateService.getBidInfo(winnerId);
            if (winnerBid != null) {
                // Emite o evento final
                LeilaoConcluidoEvent evento = new LeilaoConcluidoEvent(
                        auction.idAuction(),
                        winnerId,
                        auction.idVendedor(),
                        winnerBid.idComprador(),
                        auction.idCarta(),
                        valor
                );
                eventPublisher.publicarLeilaoConcluido(evento);
                
                // Limpa o robô do Redis (ele já comprou a carta)
                marketStateService.removeBid(winnerId);
            }
        }
    }
}
