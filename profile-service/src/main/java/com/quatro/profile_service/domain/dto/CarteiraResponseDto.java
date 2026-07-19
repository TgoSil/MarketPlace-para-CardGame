package com.quatro.profile_service.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarteiraResponseDto {
    double dinheiro;
    String username;
    LocalDate criadoEm;
}
