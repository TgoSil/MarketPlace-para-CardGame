package com.quatro.auth_service.kakfa;

import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.event.UsuarioEvento;

import org.springframework.kafka.core.KafkaTemplate;

@Service
public class KafkaProducer {

    public final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendUsuarioCriado(UsuarioEvento evento) {
        kafkaTemplate.send(UsuarioEvento.TOPICO_CRIADO, evento.id().toString(), evento);
    }

}
