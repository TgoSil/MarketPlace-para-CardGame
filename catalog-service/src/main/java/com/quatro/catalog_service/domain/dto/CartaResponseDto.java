package com.quatro.catalog_service.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartaResponseDto {
    
    private UUID id;
    private String nome;
    private String tipo;
    private String raridade;
    private Integer vida;
    private String descricao;
    private String imagemUrl;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

}