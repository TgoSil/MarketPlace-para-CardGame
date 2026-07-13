package com.quatro.inventory_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quatro.inventory_service.domain.entity.Inventario;
import com.quatro.inventory_service.domain.entity.UsuarioCartaId;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, UsuarioCartaId> {

    // Retorna todo o inventário (todas as cartas) de um usuário específico
    List<Inventario> findAllByUserId(UUID userId);

    // O Spring Data JPA já nos dá o findById(UsuarioCartaId), 
    // mas às vezes é mais prático buscar passando os UUIDs diretamente.
    Optional<Inventario> findByUserIdAndCartaId(UUID userId, UUID cartaId);

    // Caso precise deletar o inventário inteiro de um usuário (ex: conta excluída)
    void deleteAllByUserId(UUID userId);
}