package com.quatro.catalog_service.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartaRequestDto {

    @NotBlank(message = "O nome da carta é obrigatório")
    private String nome;

    @NotBlank(message = "O tipo da carta é obrigatório")
    private String tipo;

    // Campos opcionais não precisam de anotações de validação de obrigatoriedade
    private String raridade;

    @NotNull(message = "A vida da carta é obrigatória")
    @Min(value = 0, message = "A vida não pode ser um valor negativo")
    private Integer vida;

    private String descricao;

    private String imagemUrl;
}