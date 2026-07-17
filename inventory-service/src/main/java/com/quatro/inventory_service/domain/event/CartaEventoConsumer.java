package com.quatro.inventory_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.inventory_service.domain.entity.Carta;
import com.quatro.inventory_service.repository.CartaRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CartaEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(CartaEventoConsumer.class);

    private final CartaRepository cartaRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "CARTA_EVENTO", groupId = "inventory-service")
    public void consumir(String payload) {
        try {
            CartaEvento evento = objectMapper.readValue(payload, CartaEvento.class);
            log.info("Evento de carta recebido: {}", evento);

            switch (evento.tipoEvento()) {
                case CartaEvento.CRIADA, CartaEvento.ATUALIZADA ->
                        cartaRepository.save(new Carta(evento.idCarta(), evento.nome()));
                case CartaEvento.DELETADA ->
                        cartaRepository.deleteById(evento.idCarta());
                default ->
                        log.warn("Tipo de evento desconhecido, ignorando: {}", evento.tipoEvento());
            }
        } catch (Exception e) {
            log.error("Erro ao processar o payload de CartaEvento: {} - Motivo: {}", payload, e.getMessage());
        }
    }
}