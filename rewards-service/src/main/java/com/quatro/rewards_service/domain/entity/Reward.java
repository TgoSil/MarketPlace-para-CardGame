package com.quatro.rewards_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rewards")
public class Reward {
    @Id
    @Column(name = "dia_ciclo")
    private Integer diaCiclo;

    @Column(name = "tipo_reward", nullable = false)
    private String tipoReward;

    @Column(name = "quantidade_moedas_base")
    private Integer quantidadeMoedasBase;

    @Column(name = "tier_pacote_base")
    private String tierPacoteBase;
}