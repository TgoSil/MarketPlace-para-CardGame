package com.quatro.catalog_service.domain.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartaEventDto {
    // Pode ser: "CRIADA", "ATUALIZADA" ou "DELETADA"
    private String acao; 
    
    // Os dados completos da carta (vai ser nulo se a ação for DELETADA)
    private CartaResponseDto carta; 
    
    // O ID da carta sempre viaja, muito útil para o Inventário saber quem remover
    private UUID cartaId; 
}