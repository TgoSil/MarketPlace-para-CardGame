package com.quatro.rewards_service.domain.repository;

import com.quatro.rewards_service.domain.entity.CartaRecebida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CartaRecebidaRepository extends JpaRepository<CartaRecebida, UUID> {
}