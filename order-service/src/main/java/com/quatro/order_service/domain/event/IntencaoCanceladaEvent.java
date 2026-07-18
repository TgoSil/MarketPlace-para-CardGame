package com.quatro.order_service.domain.event;

import java.util.UUID;

/**
 * Evento publicado no tópico INTENCAO_CANCELADA quando uma ordem é cancelada pelo
 * usuário ou expirada pelo scheduler. O Matching Service consome este evento para
 * remover a intenção do motor de casamento.
 */
public record IntencaoCanceladaEvent(
        String tipo,       // "AUCTION" ou "BID"
        UUID orderId,      // idAuction ou idBid
        UUID idCarta,
        UUID idUser,
        String motivo      // "CANCELADA_USUARIO" ou "EXPIRADA"
) {
    public static final String TOPICO = "INTENCAO_CANCELADA";
}
