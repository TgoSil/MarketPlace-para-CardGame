package com.quatro.profile_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecompensaDinheiroConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaDinheiroConsumer.class);

    private final ProfileService profileService;

    @KafkaListener(topics = "RECOMPENSA_DINHEIRO", groupId = "profile-service")
    public void consumir(RecompensaDinheiroEvent evento){
        log.info("Evento de recompensa recebido: {}", evento);
        CarteiraRequestDto request = CarteiraRequestDto.builder()
                                            .dinheiro(evento.quantidade()*100)
                                            .build();
        profileService.adicionarNaCarteira(evento.idUser(), request);
    }
}