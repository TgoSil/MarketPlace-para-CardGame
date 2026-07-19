package com.quatro.settlement_service.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record LeilaoEvent(
        UUID idLeilao,
        UUID idBidVencedora,
        UUID idVendedor,
        UUID idComprador,
        UUID idCarta,
        BigDecimal valorFechamento
) {
    public static final String TOPICO = "LEILAO_CONCLUIDO";
}