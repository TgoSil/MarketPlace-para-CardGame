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
                bid = marketStateService.getBidInfo(bidId);
                if (bid == null || bid.salasAtuais().isEmpty()) continue;
            }

            for (UUID auctionId : bid.salasAtuais()) {
                AuctionInfo auction = marketStateService.getAuctionInfo(auctionId);
                if (auction == null) continue;

                UUID topBidder = marketStateService.getTopBidder(auctionId);
                if (topBidder != null && topBidder.equals(bid.idBid())) {
                    // Já estou ganhando, não faço nada
                    continue;
                }

                // Estou perdendo. Vejo quanto o líder está pagando
                BigDecimal topBidValue = marketStateService.getTopBidValue(auctionId);
                if (topBidValue == null) topBidValue = auction.precoMinimo();

                // Incremento de 1 dólar pra vencer
                BigDecimal novoLance = topBidValue.add(BigDecimal.ONE);

                // Se eu tenho limite pra cobrir, eu cubro!
                if (bid.limitePagamento().compareTo(novoLance) >= 0) {
                    marketStateService.updateBidInAuction(auctionId, bid.idBid(), novoLance);
                    log.info("Robô {} cobriu o lance na sala {} com {}", bid.idBid(), auctionId, novoLance);
                } else {
                    // Não tenho dinheiro pra essa briga. Poderia fazer ZREM e sair da sala.
                    // Para simplificar, vou apenas continuar perdendo (fica no fundo da fila).
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
