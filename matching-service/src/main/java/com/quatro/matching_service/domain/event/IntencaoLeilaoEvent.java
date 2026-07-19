package com.quatro.matching_service.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IntencaoLeilaoEvent(
        String tipo,
        UUID orderId,
        UUID idCarta,
        UUID idUser,
        BigDecimal precoMinimo,
        BigDecimal precoTeto,
        BigDecimal limitePagamento,
        String perfilCompra,
        Instant expiraEm
) {
}
