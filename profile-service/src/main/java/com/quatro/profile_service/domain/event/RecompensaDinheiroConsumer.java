package com.quatro.profile_service.domain.event;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.domain.entity.ProcessedEvent;
import com.quatro.profile_service.domain.repository.ProcessedEventRepository;
import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecompensaDinheiroConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaDinheiroConsumer.class);

    private final ProfileService profileService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "RECOMPENSA_DINHEIRO", groupId = "profile-service")
    public void consumir(String payload){
        try {
            RecompensaDinheiroEvent evento = objectMapper.readValue(payload, RecompensaDinheiroEvent.class);
            log.info("Evento de recompensa recebido: {}", evento);

            // GUARDA DE IDEMPOTÊNCIA
            if (evento.eventId() != null && processedEventRepository.existsById(evento.eventId())) {
                log.info("Evento {} já processado, ignorando duplicata.", evento.eventId());
                return;
            }
            
            CarteiraRequestDto request = CarteiraRequestDto.builder()
                                                .dinheiro(evento.quantidade() * 100)
                                                .build();
            profileService.adicionarNaCarteira(evento.idUser(), request);

            // Registra como processado (na mesma transação @Transactional)
            if (evento.eventId() != null) {
                processedEventRepository.save(ProcessedEvent.builder()
                        .eventId(evento.eventId())
                        .tipo("RECOMPENSA_DINHEIRO")
                        .processadoEm(LocalDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Erro ao converter payload ou adicionar recompensa: {}", payload, e);
        }
    }
}