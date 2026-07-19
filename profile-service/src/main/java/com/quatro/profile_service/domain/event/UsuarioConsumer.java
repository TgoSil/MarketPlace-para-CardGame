package com.quatro.profile_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class UsuarioConsumer {
    private static final Logger log = LoggerFactory.getLogger(UsuarioConsumer.class);

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "USUARIO_CRIADO", groupId = "profile-service")
    public void consumir(String payload){
        try{
            UsuarioEvento evento = objectMapper.readValue(payload, UsuarioEvento.class);
            log.info("Evento de usuário recebido: {}", evento);
            try{
                profileService.criarCarteira(evento.id(), evento.username());
            }catch(Exception e){
                log.warn("Usuário já existe, ignorando: {}", evento);
            }
            
        }catch(Exception e){
            log.error("Erro ao processar o payload: {} - Motivo: {}", payload, e.getMessage());
        }
        
    }
}
