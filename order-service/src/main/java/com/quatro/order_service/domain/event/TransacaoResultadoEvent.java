package com.quatro.order_service.domain.event;

import java.util.UUID;

public record TransacaoResultadoEvent(
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
}
