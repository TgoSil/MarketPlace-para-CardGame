package com.quatro.order_service.domain.event;

import java.util.UUID;

public record IntencaoCanceladaEvent(
        String tipo,
        UUID orderId,
        UUID idCarta,
        UUID idUser,
        String motivo
) {
    public static final String TOPICO = "INTENCAO_CANCELADA";
}
