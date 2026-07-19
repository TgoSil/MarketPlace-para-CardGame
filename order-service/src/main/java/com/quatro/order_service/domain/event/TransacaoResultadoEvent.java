package com.quatro.order_service.domain.event;

import java.util.UUID;

public record TransacaoResultadoEvent(
        UUID transacaoId,
        UUID ordemCompraId,     // corresponde ao ID da Bid
        UUID ordemVendaId,      // corresponde ao ID da Auction
        UUID compradorId,
        UUID vendedorId,
        UUID cartaId,
        Integer preco,
        Integer quantidade,
        String status,          // "CONCLUIDA" ou "FALHA"
        String razaoFalha       // null se sucesso; descrição do erro se falha
) {
    public static final String TOPICO = "TRANSACAO_RESULTADO";
}
