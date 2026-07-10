package com.quatro.auth_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quatro.auth_service.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

    public Optional<User> findByEmail(String email);

    public Boolean existsByEmail(String email);

    public Boolean existsByUsername(String username);

}
