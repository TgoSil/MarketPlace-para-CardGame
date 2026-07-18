package com.quatro.catalog_service.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name="cartas")
public class cartas {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "carta_id", updatable = false, nullable = false)
    private UUID cartaId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String raridade;

    @Column(nullable = false)
    private Integer vida;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url", length = 255)
    private String imagemUrl;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

}