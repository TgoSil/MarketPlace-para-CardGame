package com.quatro.auth_service.domain.event;

import java.util.UUID;

public record UsuarioEvento(UUID id) {
    public static final String TOPICO_CRIADO = "USUARIO_CRIADO";
}