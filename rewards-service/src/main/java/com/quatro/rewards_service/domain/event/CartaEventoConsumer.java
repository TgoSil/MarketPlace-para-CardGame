package com.quatro.rewards_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.rewards_service.domain.entity.Carta;
import com.quatro.rewards_service.domain.event.CartaEvento;
import com.quatro.rewards_service.domain.repository.CartaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartaEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(CartaEventoConsumer.class);

    private final CartaRepository cartaRepository;

    @KafkaListener(topics = "CARTA_EVENTO", groupId = "rewards-service")
    public void consumir(CartaEvento evento) {
        log.info("Evento de carta recebido: {}", evento);

        switch (evento.tipoEvento()) {
            case CartaEvento.CRIADA, CartaEvento.ATUALIZADA ->
                    cartaRepository.save(new Carta(evento.idCarta(), evento.nome(), evento.raridade()));
            case CartaEvento.DELETADA ->
                    cartaRepository.deleteById(evento.idCarta());
            default ->
                    log.warn("Tipo de evento desconhecido, ignorando: {}", evento.tipoEvento());
        }
    }
}