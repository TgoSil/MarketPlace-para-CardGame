package com.quatro.rewards_service.domain.event;

import java.util.UUID;

public record RecompensaDinheiroEvent(UUID eventId, UUID idUser, int quantidade) {
}