package com.quatro.matching_service.domain.event;

import java.util.UUID;

public record IntencaoCanceladaEvent(
        String tipo,
        UUID orderId,
        UUID idCarta,
        UUID idUser,
        String motivo
) {
}
