package com.quatro.order_service.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no tópico INTENCAO_LEILAO quando uma nova Auction ou Bid é criada.
 * O Matching Service consome este evento para incluir a intenção no motor de casamento.
 */
public record IntencaoLeilaoEvent(
        String tipo,           // "AUCTION" ou "BID"
        UUID orderId,          // idAuction ou idBid
        UUID idCarta,
        UUID idUser,
        BigDecimal precoMinimo,    // Auction: preço mínimo aceito | Bid: null
        BigDecimal precoTeto,      // Auction: preço buyout | Bid: null
        BigDecimal limitePagamento,// Bid: limite de pagamento | Auction: null
        String perfilCompra,       // Bid: perfil do bot | Auction: null
        Instant expiraEm
) {
    public static final String TOPICO = "INTENCAO_LEILAO";
}
