package com.quatro.profile_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioConsumer {
    private static final Logger log = LoggerFactory.getLogger(RecompensaDinheiroConsumer.class);

    private final ProfileService profileService;

    @KafkaListener(topics = "USUARIO_CRIADO", groupId = "profile-service")
    public void consumir(UsuarioEvento evento){
        log.info("Evento de usuário recebido: {}", evento);
        try{
            profileService.criarCarteira(evento.id());
        }catch(Exception e){
            log.warn("Usuário já existe, ignorando: {}", evento);
        }
        
    }
}
