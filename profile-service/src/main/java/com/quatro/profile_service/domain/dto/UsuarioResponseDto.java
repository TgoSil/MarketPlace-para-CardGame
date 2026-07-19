package com.quatro.profile_service.domain.dto;

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
public class UsuarioResponseDto {
    private UUID id;
    private String username;
    private double dinheiro;
    private Instant criadoEm;
}