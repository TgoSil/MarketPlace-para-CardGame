package com.quatro.rewards_service.domain.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacoteProbabilidadeId implements Serializable {
    private String tierPacote;
    private String raridade;
}