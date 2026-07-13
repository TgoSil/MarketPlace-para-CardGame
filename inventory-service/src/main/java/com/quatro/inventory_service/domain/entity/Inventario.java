package com.quatro.inventory_service.domain.entity;

import java.util.UUID;

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
@Table(name="inventarios")
@IdClass(UsuarioCartaId.class)
public class Inventario {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "carta_id")
    private UUID cartaId;

    @Column(nullable=false)
    private Integer quantidade;

}