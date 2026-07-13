package com.quatro.inventory_service.domain.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioRequestDto {
        
    @NotNull(message = "O ID da carta é obrigatório")
    private UUID cartaId;
        
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private Integer quantidade;
}
