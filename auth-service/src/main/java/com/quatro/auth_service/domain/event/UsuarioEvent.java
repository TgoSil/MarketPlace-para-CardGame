package com.quatro.auth_service.domain.event;

import java.util.UUID;

public record UsuarioEvent(UUID id, String username) {
    public static final String CRIADO = "CRIADO";
}