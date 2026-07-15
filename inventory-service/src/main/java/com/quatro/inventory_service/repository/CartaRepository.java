package com.quatro.inventory_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quatro.inventory_service.domain.entity.Carta;

@Repository
public interface CartaRepository extends JpaRepository<Carta, UUID> {
}