package com.quatro.rewards_service.domain.repository;

import com.quatro.rewards_service.domain.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface LoginRepository extends JpaRepository<Login, UUID> {

    boolean existsByIdUserAndDataLogin(UUID idUser, LocalDate dataLogin);
}