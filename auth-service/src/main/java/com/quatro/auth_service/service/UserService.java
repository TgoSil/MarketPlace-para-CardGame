package com.quatro.auth_service.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.dto.LoginRequestDto;
import com.quatro.auth_service.domain.entity.User;
import com.quatro.auth_service.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository authRepository;

    public UserService(UserRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Optional<User> findByEmail(LoginRequestDto loginRequestDto) {
        return authRepository.findByEmail(loginRequestDto.getEmail());
    }



}
