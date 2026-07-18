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
public class AuctionRequestDto {
    private UUID idCarta;
    private BigDecimal precoMinimo;
    private BigDecimal precoTeto;
    private Instant expiraEm;
}
