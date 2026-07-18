package com.quatro.matching_service.domain.redis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuctionInfo(
        UUID idAuction,
        UUID idVendedor,
        UUID idCarta,
        BigDecimal precoMinimo,
        BigDecimal precoTeto,
        Instant expiraEm
) {
}
