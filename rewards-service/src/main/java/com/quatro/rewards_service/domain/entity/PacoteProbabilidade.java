package com.quatro.rewards_service.domain.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "pacote_probabilidades")
@IdClass(PacoteProbabilidadeId.class)
public class PacoteProbabilidade {
    @Id
    @Column(name = "tier_pacote")
    private String tierPacote;

    @Id
    @Column(name = "raridade")
    private String raridade;

    @Column(nullable = false)
    private BigDecimal porcentagem;
}