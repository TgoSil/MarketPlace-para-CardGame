package com.quatro.auth_service.domain.dto;

import java.util.UUID;

public record UserCreatedRecord (UUID id, String email, String cargo) {
}
