package com.quatro.matching_service.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record LeilaoConcluidoEvent(
        UUID idLeilao,
        UUID idBidVencedora,
        UUID idVendedor,
        UUID idComprador,
        UUID idCarta,
        BigDecimal valorFechamento
) {
}
