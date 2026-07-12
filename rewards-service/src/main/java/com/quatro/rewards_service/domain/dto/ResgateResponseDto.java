package com.quatro.rewards_service.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResgateResponseDto {
    private String tipoReward;
    private Integer moedasRecebidas;
    private String tierPacote;
    private List<CartaDto> cartas;
    private Integer streak;
    private Integer diaCiclo;
    private Integer ciclo;
}