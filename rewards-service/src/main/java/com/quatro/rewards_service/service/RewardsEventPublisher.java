package com.quatro.rewards_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.quatro.rewards_service.domain.event.RecompensaCartaEvent;
import com.quatro.rewards_service.domain.event.RecompensaDinheiroEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardsEventPublisher {

    public static final String TOPICO_RECOMPENSA_DINHEIRO = "RECOMPENSA_DINHEIRO";
    public static final String TOPICO_RECOMPENSA_CARTA = "RECOMPENSA_CARTA";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarRecompensaDinheiro(RecompensaDinheiroEvent evento) {
        kafkaTemplate.send(TOPICO_RECOMPENSA_DINHEIRO, evento.idUser().toString(), evento);
    }

    public void publicarRecompensaCarta(RecompensaCartaEvent evento) {
        kafkaTemplate.send(TOPICO_RECOMPENSA_CARTA, evento.idUser().toString(), evento);
    }
}