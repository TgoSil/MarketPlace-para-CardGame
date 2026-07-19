package com.quatro.inventory_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quatro.inventory_service.domain.entity.Inventario;
import com.quatro.inventory_service.domain.entity.UsuarioCartaId;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, UsuarioCartaId> {

    Optional<Inventario> findByUserIdAndCartaId(UUID userId, UUID cartaId);

    void deleteAllByUserId(UUID userId);

    @Query("SELECT i FROM Inventario i JOIN FETCH i.carta WHERE i.userId = :userId")
    List<Inventario> findAllByUserId(@Param("userId") UUID userId);

}