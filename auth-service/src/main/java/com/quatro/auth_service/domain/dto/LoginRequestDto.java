package com.quatro.auth_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequestDto {

    @Email
    @NotBlank(message="O campo 'email' não pode ficar em branco.")
    private String email;

    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @NotBlank(message="O campo 'senha' não pode ficar em branco.")
    private String senha;

}
