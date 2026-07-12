package com.quatro.auth_service.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.quatro.auth_service.domain.dto.LoginRequestDto;
import com.quatro.auth_service.domain.dto.LoginResponseDto;
import com.quatro.auth_service.domain.dto.RegisterRequestDto;
import com.quatro.auth_service.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Efetua login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
        @Valid @RequestBody LoginRequestDto loginRequestDto) {
            Optional<String> tokenOptional = authService.authenticate(loginRequestDto);
            if (tokenOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String token = tokenOptional.get();
            return ResponseEntity.ok(new LoginResponseDto(token));
    }

    // @Operation(summary = "Valida token do usuário")
    // @GetMapping("/validate")
    // public ResponseEntity<Void> validate(
    //     @RequestHeader("Authorization") String authHeader) {
    //         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    //         }
    //         return authService.validateToken(authHeader.substring(7))
    //         ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    //     }

    @Operation(summary = "Registra usuário")
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(
        @Valid @RequestBody RegisterRequestDto registerRequestDto) {
            String token = authService.register(registerRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDto(token));
        }
}
