package com.quatro.rewards_service.domain.entity;

import java.time.LocalDate;
import java.time.LocalTime;
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
@Table(name = "login")
public class Login {
    @Id
    @Column(name = "id_login")
    private UUID idLogin;

    @Column(name = "id_user", nullable = false)
    private UUID idUser;

    @Column(name = "dia_ciclo", nullable = false)
    private Integer diaCiclo;

    @Column(name = "data_login", nullable = false)
    private LocalDate dataLogin;

    @Column(name = "horario_login", nullable = false)
    private LocalTime horarioLogin;

    @Column(name = "streak", nullable = false)
    private Integer streak;

    @Column(name = "ciclo", nullable = false)
    private Integer ciclo;

    @Column(name = "moedas_recebidas")
    private Integer moedasRecebidas;

    @Column(name = "tier_pacote_recebido")
    private String tierPacoteRecebido;
}