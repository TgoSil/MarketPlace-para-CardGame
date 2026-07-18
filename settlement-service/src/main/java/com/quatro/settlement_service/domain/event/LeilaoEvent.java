package com.quatro.settlement_service.domain.event;

import java.util.UUID;

public record LeilaoEvent(
        UUID ordemCompraId,
        UUID ordemVendaId,
        UUID compradorId,
        UUID vendedorId,
        UUID cartaId,
        Integer preco,
        Integer quantidade
) {
    public static final String TOPICO = "LEILAO_CONCLUIDO";
}