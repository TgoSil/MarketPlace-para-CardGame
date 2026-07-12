package com.quatro.rewards_service.domain.entity;

import java.util.UUID;
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
@Table(name = "cartas_recebidas_resgate")
public class CartaRecebida {
    @Id
    private UUID id;

    @Column(name = "id_login", nullable = false)
    private UUID idLogin;

    @Column(name = "id_carta", nullable = false)
    private UUID idCarta;

    @Column(name = "raridade_sorteada", nullable = false)
    private String raridadeSorteada;
}