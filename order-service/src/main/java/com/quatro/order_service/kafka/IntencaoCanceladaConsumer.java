package com.quatro.order_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quatro.order_service.domain.event.IntencaoCanceladaEvent;
import com.quatro.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntencaoCanceladaConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @KafkaListener(topics = "INTENCAO_CANCELADA", groupId = "order-service")
    public void consumirCancelamento(String payload) {
        try {
            IntencaoCanceladaEvent event = objectMapper.readValue(payload, IntencaoCanceladaEvent.class);
            log.info("Recebido IntencaoCanceladaEvent. orderId={}, motivo={}", event.orderId(), event.motivo());

            orderService.marcarComoCanceladaExterna(event.orderId(), event.motivo());

        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar IntencaoCanceladaEvent", e);
        }
    }
}
