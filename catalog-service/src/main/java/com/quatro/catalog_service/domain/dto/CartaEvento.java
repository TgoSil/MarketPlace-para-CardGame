package com.quatro.catalog_service.domain.dto;

import java.util.UUID;

public record CartaEvento(UUID idCarta, String nome, String raridade, String tipoEvento) {
    public static final String CRIADA = "CRIADA";
    public static final String ATUALIZADA = "ATUALIZADA";
    public static final String DELETADA = "DELETADA";
}