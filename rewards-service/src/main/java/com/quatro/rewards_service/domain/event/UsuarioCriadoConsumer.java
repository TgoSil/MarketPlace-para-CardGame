package com.quatro.rewards_service.domain.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.quatro.rewards_service.domain.entity.UserStreak;
import com.quatro.rewards_service.domain.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(UsuarioCriadoConsumer.class);

    private final UserStreakRepository userStreakRepository;

    @KafkaListener(
            topics = "USUARIO_CRIADO",
            groupId = "rewards-service",
            properties = {"spring.json.value.default.type=com.quatro.rewards_service.domain.event.UsuarioEvento"}
    )
    public void consumir(UsuarioEvento evento) {
        log.info("Evento de usuário criado recebido: {}", evento);

        if (userStreakRepository.existsById(evento.id())) {
            log.info("Streak já existe para o usuário {}, ignorando", evento.id());
            return;
        }

        userStreakRepository.save(UserStreak.builder()
                .idUser(evento.id())
                .diaCiclo(1)
                .ciclo(0)
                .streak(0)
                .dataUltimoLogin(null)
                .build());

        log.info("Streak inicial criado para o usuário {}", evento.id());
    }
}