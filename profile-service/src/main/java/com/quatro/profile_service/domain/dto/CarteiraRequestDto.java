package com.quatro.profile_service.domain.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarteiraRequestDto {
    @Min(value = 0, message = "O dinheiro não pode ser negativo.")
    private Integer dinheiro;
}