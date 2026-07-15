package com.quatro.inventory_service.domain.entity;

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
@Table(name = "cartas")
public class Carta {
    @Id
    @Column(name = "id_carta")
    private UUID idCarta;

    @Column(nullable = false)
    private String nome;
}