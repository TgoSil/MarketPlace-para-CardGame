package com.quatro.inventory_service.domain.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @Column(name = "id_carta")
    private UUID cartaId;

    @Column(nullable=false)
    private Integer quantidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carta", referencedColumnName = "id_carta", insertable = false, updatable = false)
    private Carta carta;

}