package com.quatro.auth_service.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.dto.LoginRequestDto;
import com.quatro.auth_service.util.JwtUtil;

import io.jsonwebtoken.JwtException;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDto loginRequestDto) {
        Optional<String> tokenOptional = userService.findByEmail(loginRequestDto)
        .filter(u -> passwordEncoder.matches(loginRequestDto.getSenha(), u.getSenha()))
        .map(u -> jwtUtil.generateToken(u.getEmail(), u.getCargo()));
        return tokenOptional;
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        }
        catch(JwtException e) {
            return false;
        }
    }

}
