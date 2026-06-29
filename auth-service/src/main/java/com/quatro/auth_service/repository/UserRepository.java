package com.quatro.auth_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quatro.auth_service.domain.entity.User;

public interface UserRepository extends JpaRepository<UUID, User>{

    public Optional<User> findByEmail(String email);

}
