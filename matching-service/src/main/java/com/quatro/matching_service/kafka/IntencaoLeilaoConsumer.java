package com.quatro.matching_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quatro.matching_service.domain.event.IntencaoLeilaoEvent;
import com.quatro.matching_service.domain.redis.AuctionInfo;
import com.quatro.matching_service.domain.redis.BidInfo;
import com.quatro.matching_service.domain.redis.MarketStateService;
import com.quatro.matching_service.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntencaoLeilaoConsumer {

    private final ObjectMapper objectMapper;
    private final MarketStateService marketStateService;
    private final RoutingService routingService;

    @KafkaListener(topics = "INTENCAO_LEILAO", groupId = "matching-service")
    public void consumirIntencao(String payload) {
        try {
            IntencaoLeilaoEvent event = objectMapper.readValue(payload, IntencaoLeilaoEvent.class);
            log.info("Recebida nova intencao. tipo={}, orderId={}", event.tipo(), event.orderId());

            if ("AUCTION".equals(event.tipo())) {
                AuctionInfo info = new AuctionInfo(
                        event.orderId(),
                        event.idUser(),
                        event.idCarta(),
                        event.precoMinimo(),
                        event.precoTeto(),
                        event.expiraEm()
                );
                marketStateService.saveAuctionInfo(info);
            } else if ("BID".equals(event.tipo())) {
                BidInfo info = new BidInfo(
                        event.orderId(),
                        event.idUser(),
                        event.idCarta(),
                        event.limitePagamento(),
                        event.perfilCompra(),
                        event.expiraEm(),
                        new ArrayList<>() // Vai ser preenchido pelo RoutingService
                );
                marketStateService.saveBidInfo(info);
                routingService.rotearNovoBid(info);
            }

        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar IntencaoLeilaoEvent", e);
        }
    }
}
