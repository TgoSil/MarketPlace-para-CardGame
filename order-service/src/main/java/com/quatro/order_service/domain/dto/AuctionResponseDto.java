package com.quatro.order_service.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionResponseDto {
    private UUID idAuction;
    private UUID idCarta;
    private UUID idUser;
    private BigDecimal precoMinimo;
    private BigDecimal precoTeto;
    private Instant criadoEm;
    private Instant expiraEm;
    private String status;
}
