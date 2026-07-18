package com.quatro.order_service.domain.event;

import java.util.UUID;

/**
 * Espelha o TransacaoEvent publicado pelo Settlement Service no tópico TRANSACAO_RESULTADO.
 * O Order Service consome este evento para atualizar o status das ordens originais.
 */
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
