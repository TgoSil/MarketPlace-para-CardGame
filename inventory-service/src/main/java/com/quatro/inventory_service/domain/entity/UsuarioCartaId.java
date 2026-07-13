package com.quatro.inventory_service.domain.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCartaId implements Serializable {
    private UUID userId;
    private UUID cartaId;
}