package com.quatro.matching_service.domain.redis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BidInfo(
        UUID idBid,
        UUID idComprador,
        UUID idCarta,
        BigDecimal limitePagamento,
        String perfilCompra,
        java.time.Instant expiraEm,
        List<UUID> salasAtuais // As auctions nas quais este bid está competindo
) {
}
