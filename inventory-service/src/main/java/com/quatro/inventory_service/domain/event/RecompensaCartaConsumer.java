package com.quatro.inventory_service.domain.event;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.entity.ProcessedEvent;
import com.quatro.inventory_service.domain.event.RecompensaCartaEvent.CartaGanha;
import com.quatro.inventory_service.domain.repository.ProcessedEventRepository;
import com.quatro.inventory_service.service.InventarioService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecompensaCartaConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaCartaConsumer.class);

    private final InventarioService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "RECOMPENSA_CARTA", groupId = "inventory-service")
    public void consumir(String payload){
        try {
            RecompensaCartaEvent evento = objectMapper.readValue(payload, RecompensaCartaEvent.class);
            log.info("Evento de recompensa recebido: {}", evento);

            if (evento.eventId() != null && processedEventRepository.existsById(evento.eventId())) {
                log.info("Evento {} já processado, ignorando duplicata.", evento.eventId());
                return;
            }
            
            for(CartaGanha carta : evento.cartas()){
                InventarioRequestDto inventario = InventarioRequestDto.builder()
                    .cartaId(carta.cartaId())
                    .quantidade(carta.quantidade())
                    .build();
                inventoryService.adicionarOuAtualizarCarta(evento.idUser(), inventario);
            }

            if (evento.eventId() != null) {
                processedEventRepository.save(ProcessedEvent.builder()
                        .eventId(evento.eventId())
                        .tipo("RECOMPENSA_CARTA")
                        .processadoEm(LocalDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Erro ao converter payload ou processar recompensa: {} - Motivo: {}", payload, e.getMessage());
        }
    }
}
