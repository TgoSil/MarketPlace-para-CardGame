package com.quatro.settlement_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacoes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ordem_compra_id", nullable = false)
    private UUID ordemCompraId;

    @Column(name = "ordem_venda_id", nullable = false)
    private UUID ordemVendaId;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "carta_id", nullable = false)
    private UUID cartaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private Integer preco;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "razao_falha", columnDefinition = "TEXT")
    private String razaoFalha;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}