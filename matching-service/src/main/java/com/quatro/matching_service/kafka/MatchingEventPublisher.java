package com.quatro.matching_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quatro.matching_service.domain.event.IntencaoCanceladaEvent;
import com.quatro.matching_service.domain.event.LeilaoConcluidoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publicarLeilaoConcluido(LeilaoConcluidoEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("LEILAO_CONCLUIDO", event.idLeilao().toString(), payload);
            log.info("Publicado LeilaoConcluidoEvent para auction={}", event.idLeilao());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar LeilaoConcluidoEvent", e);
        }
    }

    public void publicarIntencaoCancelada(IntencaoCanceladaEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("INTENCAO_CANCELADA", event.orderId().toString(), payload);
            log.info("Publicado IntencaoCanceladaEvent para order={}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar IntencaoCanceladaEvent", e);
        }
    }
}
