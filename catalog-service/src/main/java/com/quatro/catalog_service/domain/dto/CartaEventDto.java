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
    private String acao; 
    
    private CartaResponseDto carta; 
    
    private UUID cartaId; 
}