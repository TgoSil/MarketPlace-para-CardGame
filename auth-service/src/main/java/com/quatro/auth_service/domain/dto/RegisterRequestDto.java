package com.quatro.auth_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    
    @Size(min = 3, message = "O nome de usuário deve ter ao menos 3 caracteres")
    @Size(max = 16, message = "O nome de usuário não pode ter mais de 16 caracteres")
    @NotBlank(message = "O campo de nome de usuário não pode ficar em branco")
    private String username;

    @Email(message = "Utilize um email válido")
    @NotBlank(message = "O campo de email não pode ficar em branco")
    private String email;

    @Size(min = 8, message = "A senha deve conter ao menos 8 caracteres")
    @NotBlank(message = "O campo de senha não pode ficar em branco")
    private String senha;

}
