package com.quatro.rewards_service.domain.event;

import java.util.List;
import java.util.UUID;

public record RecompensaCartaEvent(UUID idUser, List<CartaGanha> cartas) {
    public record CartaGanha(UUID cartaId, int quantidade) {
    }
}