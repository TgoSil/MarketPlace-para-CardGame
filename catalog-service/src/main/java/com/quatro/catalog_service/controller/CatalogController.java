package com.quatro.catalog_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import com.quatro.catalog_service.domain.dto.CartaRequestDto;
import com.quatro.catalog_service.domain.dto.CartaResponseDto;
import com.quatro.catalog_service.service.CatalogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    // 1. Criar uma nova carta
    // Rota: POST http://localhost:4004/catalog/carta
    @PostMapping("/carta")
    public ResponseEntity<CartaResponseDto> criarCarta(@Valid @RequestBody CartaRequestDto requestDto, @RequestHeader("User-cargo") String cargo) {
        // Passamos 'null' como ID para forçar a criação
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CartaResponseDto novaCarta = catalogService.salvarOuAtualizarCarta(null, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCarta);
    }

    // 2. Editar uma carta existente
    // Rota: PUT http://localhost:4004/catalog/carta/123e4567-e89b-12d3-a456-426614174000
    @PutMapping("/carta/{id}")
    public ResponseEntity<CartaResponseDto> editarCarta(
            @PathVariable UUID id, 
            @Valid @RequestBody CartaRequestDto requestDto,
            @RequestHeader("User-cargo") String cargo) {

        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Passamos o ID recebido na URL para forçar a atualização daquela carta específica
        CartaResponseDto cartaAtualizada = catalogService.salvarOuAtualizarCarta(id, requestDto);
        
        // Retorna HTTP 200 (OK) quando a atualização é bem-sucedida
        return ResponseEntity.ok(cartaAtualizada);
    
    }

    // 2. Exibir e Filtrar cartas
    // Rotas possíveis:
    // GET http://localhost:4004/catalog/carta
    // GET http://localhost:4004/catalog/carta?nome=Dragao
    // GET http://localhost:4004/catalog/carta?raridade=Rara
    @GetMapping("/carta")
    public ResponseEntity<List<CartaResponseDto>> listarOuFiltrarCartas(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String raridade,
            @RequestParam(required = false) String tipo) {
        
        List<CartaResponseDto> cartas = catalogService.filtrarCartas(nome, raridade, tipo);
        
        // Retorna HTTP 200 (OK) com a lista resultante (que pode ser vazia, mas não dará erro)
        return ResponseEntity.ok(cartas);
    }

    // 3. Remover uma carta pelo ID
    // Rota: DELETE http://localhost:4004/catalog/carta/123e4567-e89b-12d3-a456-426614174000
    @DeleteMapping("/carta/{cartaId}")
    public ResponseEntity<Void> removerCarta(@PathVariable UUID cartaId, @RequestHeader("User-cargo") String cargo) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catalogService.removerCarta(cartaId);
        
        // Retorna HTTP 204 (No Content) avisando que deu certo, mas a resposta não tem corpo
        return ResponseEntity.noContent().build();
    }
}