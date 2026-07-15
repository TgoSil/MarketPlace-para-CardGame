package com.quatro.inventory_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.event.RecompensaCartaEvent.CartaGanha;
import com.quatro.inventory_service.service.InventarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecompensaCartaConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaCartaConsumer.class);

    private final InventarioService inventoryService;

    @KafkaListener(topics = "RECOMPENSA_CARTA", groupId = "inventory-service")
    public void consumir(RecompensaCartaEvent evento){
        log.info("Evento de recompensa recebido: {}", evento);
        for(CartaGanha carta : evento.cartas()){
            InventarioRequestDto inventario = InventarioRequestDto.builder()
                .cartaId(carta.cartaId())
                .quantidade(carta.quantidade())
                .build();
            inventoryService.adicionarOuAtualizarCarta(evento.idUser(), inventario);
        }
    }

}
