package com.quatro.order_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.quatro.order_service.domain.event.IntencaoCanceladaEvent;
import com.quatro.order_service.domain.event.IntencaoLeilaoEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarIntencaoLeilao(IntencaoLeilaoEvent evento) {
        kafkaTemplate.send(
                IntencaoLeilaoEvent.TOPICO,
                evento.idUser().toString(),
                evento
        );
    }

    public void publicarIntencaoCancelada(IntencaoCanceladaEvent evento) {
        kafkaTemplate.send(
                IntencaoCanceladaEvent.TOPICO,
                evento.idUser().toString(),
                evento
        );
    }
}
