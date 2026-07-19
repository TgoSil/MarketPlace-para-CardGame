package com.quatro.settlement_service.domain.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoRequestDto {
    
    
    private UUID id; 
    
    @NotNull(message = "O id da ordem de compra é obrigatório.")
    private UUID ordemCompraId;
    @NotNull(message = "O id da ordem de venda é obrigatório.")
    private UUID ordemVendaId;
    @NotNull(message = "O id do comprador é obrigatório.")
    private UUID compradorId;
    @NotNull(message = "O id do vendedor é obrigatório.")
    private UUID vendedorId;
    @NotNull(message = "O id da carta é obrigatório.")
    private UUID cartaId;
    @NotNull(message = "O preço é obrigatório.")
    private Integer preco;
    @Builder.Default
    @NotNull(message = "A quantidade de cartas é obrigatório.")
    private Integer quantidade = 1;
}