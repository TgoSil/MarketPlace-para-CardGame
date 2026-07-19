package com.quatro.auth_service.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.dto.LoginRequestDto;
import com.quatro.auth_service.domain.dto.RegisterRequestDto;
import com.quatro.auth_service.domain.dto.UserCreatedRecord;
import com.quatro.auth_service.domain.event.UsuarioEvento;
import com.quatro.auth_service.exceptions.EmailAlreadyExistsException;
import com.quatro.auth_service.exceptions.UsernameAlreadyExistsException;
import com.quatro.auth_service.kakfa.KafkaProducer;
import com.quatro.auth_service.util.JwtUtil;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final KafkaProducer kafkaProducer;

    public AuthService(UserService userService,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        KafkaProducer kafkaProducer) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.kafkaProducer = kafkaProducer;
    }

    public Boolean checkEmailValidity(RegisterRequestDto registerRequestDto) {
        return !userService.existsByEmail(registerRequestDto.getEmail());
    }

    public Boolean checkUsernameValidity(RegisterRequestDto registerRequestDto) {
        return !userService.existsByUsername(registerRequestDto.getUsername());
    }

    public Optional<String> authenticate(LoginRequestDto loginRequestDto) {
        Optional<String> tokenOptional = userService.findByEmail(loginRequestDto.getEmail())
        .filter(u -> passwordEncoder.matches(loginRequestDto.getSenha(), u.getSenha()))
        .map(u -> jwtUtil.generateToken(u.getId(), u.getEmail(), u.getCargo()));
        return tokenOptional;
    }

    public String register(RegisterRequestDto registerRequestDto) {
        if (!checkEmailValidity(registerRequestDto)) {
            throw new EmailAlreadyExistsException("Este email já está sendo utilizado");
        }

        if(!checkUsernameValidity(registerRequestDto)) {
            throw new UsernameAlreadyExistsException("Este username já está sendo utilizado");
        }

        UserCreatedRecord newUser = userService.save(
            registerRequestDto.getUsername(),
            registerRequestDto.getEmail(),
            passwordEncoder.encode(registerRequestDto.getSenha()),
            "Padrão");

        String token = jwtUtil.generateToken(newUser.id(), newUser.email(), newUser.cargo());

        kafkaProducer.sendUsuarioCriado(new UsuarioEvento(newUser.id(), registerRequestDto.getUsername()));

        return token;
    }

}
