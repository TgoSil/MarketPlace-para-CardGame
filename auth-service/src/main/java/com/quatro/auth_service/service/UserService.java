package com.quatro.auth_service.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.dto.LoginRequestDto;
import com.quatro.auth_service.domain.entity.User;
import com.quatro.auth_service.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(LoginRequestDto loginRequestDto) {
        return userRepository.findByEmail(loginRequestDto.getEmail());
    }



}
