package com.quatro.profile_service.domain.event;

import java.util.UUID;

public record RecompensaDinheiroEvent(UUID idUser, int quantidade) {
}