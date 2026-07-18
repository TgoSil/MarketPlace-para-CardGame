package com.quatro.catalog_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quatro.catalog_service.domain.entity.cartas;


@Repository
public interface CatalogRepository extends JpaRepository<cartas, UUID> {

    // O Spring Data JPA já nos dá o findById(Carta_Id), q
    // mas às vezes é mais prático buscar passando os UUIDs diretamente.
    Optional<cartas> findByCartaId(UUID cartaId);
    
    List<cartas> findByRaridade(String raridade);
    List<cartas> findByNome(String nome);
    List<cartas> findByTipo(String tipo);

    // Caso precise deletar uma carta do catalogo
    void deleteAllByCartaId(UUID carta_id);

    
}