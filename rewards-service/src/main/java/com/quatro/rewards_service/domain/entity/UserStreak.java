package com.quatro.rewards_service.domain.entity;

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
@Table(name = "user_streak")
public class UserStreak {
    @Id
    @Column(name = "id_user")
    private UUID idUser;

    @Column(name = "dia_ciclo", nullable = false)
    private Integer diaCiclo;

    @Column(name = "ciclo", nullable = false)
    private Integer ciclo;

    @Column(name = "streak", nullable = false)
    private Integer streak;

    @Column(name = "data_ultimo_login")
    private LocalDate dataUltimoLogin;
}