package com.quatro.auth_service.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.quatro.auth_service.domain.dto.UserCreatedRecord;
import com.quatro.auth_service.domain.entity.User;
import com.quatro.auth_service.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserCreatedRecord save(String username, String email, String senha, String cargo) {
        User newUser = User.builder()
            .username(username)
            .email(email)
            .senha(senha)
            .cargo(cargo)
            .build();
        userRepository.save(newUser);

        return new UserCreatedRecord(newUser.getId(), newUser.getEmail(), newUser.getCargo());
    }



}
