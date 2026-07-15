package com.quatro.profile_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quatro.profile_service.domain.entity.Carteira;

@Repository
public interface CarteiraRepository extends JpaRepository<Carteira, UUID>{
}
