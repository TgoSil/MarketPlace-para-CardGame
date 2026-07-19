package com.quatro.settlement_service.domain.event;

import java.util.UUID;

public record TransacaoEvent(
        UUID transacaoId,
        UUID ordemCompraId,
        UUID ordemVendaId,
        UUID compradorId,
        UUID vendedorId,
        UUID cartaId,
        Integer preco,
        Integer quantidade,
        String status,
        String razaoFalha
) {
    public static final String TOPICO = "TRANSACAO_RESULTADO";

    public static final String STATUS_CONCLUIDA = "CONCLUIDA";
    public static final String STATUS_FALHA = "FALHA";
}