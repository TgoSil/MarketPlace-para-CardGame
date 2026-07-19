package com.quatro.auth_service.domain.event;

import java.util.UUID;

public record UsuarioEvento(UUID id, String username) {
    public static final String TOPICO_CRIADO = "USUARIO_CRIADO";
}