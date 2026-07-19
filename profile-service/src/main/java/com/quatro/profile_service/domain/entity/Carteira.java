package com.quatro.profile_service.domain.entity;

import java.time.LocalDate;
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
@Table(name="carteiras")
public class Carteira {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private Integer dinheiro;

    @Column(nullable = false)
    private String username;

    @Column(name = "criado_em", nullable = false)
    private LocalDate criadoEm;
}
