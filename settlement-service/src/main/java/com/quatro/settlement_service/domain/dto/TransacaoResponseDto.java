package com.quatro.settlement_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoResponseDto {
    
    private UUID id;
    private UUID ordemCompraId;
    private UUID ordemVendaId;
    private UUID compradorId;
    private UUID vendedorId;
    private UUID cartaId;
    private Integer preco;
    private Integer quantidade;
    private String status;
    private String razaoFalha;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}