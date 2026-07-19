package com.quatro.order_service.domain.event;

import java.util.UUID;

public record IntencaoCanceladaEvent(
        String tipo,       // "AUCTION" ou "BID"
        UUID orderId,      // idAuction ou idBid
        UUID idCarta,
        UUID idUser,
        String motivo      // "CANCELADA_USUARIO" ou "EXPIRADA"
) {
    public static final String TOPICO = "INTENCAO_CANCELADA";
}
