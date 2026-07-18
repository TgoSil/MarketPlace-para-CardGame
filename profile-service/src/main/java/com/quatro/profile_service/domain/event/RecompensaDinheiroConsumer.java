package com.quatro.profile_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecompensaDinheiroConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaDinheiroConsumer.class);

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "RECOMPENSA_DINHEIRO", groupId = "profile-service")
    public void consumir(String payload){
        try {
            RecompensaDinheiroEvent evento = objectMapper.readValue(payload, RecompensaDinheiroEvent.class);
            log.info("Evento de recompensa recebido: {}", evento);
            
            CarteiraRequestDto request = CarteiraRequestDto.builder()
                                                .dinheiro(evento.quantidade() * 100)
                                                .build();
            profileService.adicionarNaCarteira(evento.idUser(), request);
        } catch (Exception e) {
            log.error("Erro ao converter payload ou adicionar recompensa: {}", payload, e);
        }
    }
}