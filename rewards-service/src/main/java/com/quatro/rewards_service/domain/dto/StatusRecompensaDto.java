package com.quatro.rewards_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusRecompensaDto {
    private boolean disponivel;
    private Integer streakAtual;
    private Integer diaCiclo;
    private Integer ciclo;
    private String tipoProximaRecompensa;
    private Integer moedasPrevistas;
    private String tierPacotePrevisto;
}