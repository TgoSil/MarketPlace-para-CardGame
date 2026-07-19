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

    // Rota: POST http://localhost:4004/catalog/carta
    @PostMapping("/carta")
    public ResponseEntity<CartaResponseDto> criarCarta(@Valid @RequestBody CartaRequestDto requestDto, @RequestHeader("User-cargo") String cargo) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CartaResponseDto novaCarta = catalogService.salvarOuAtualizarCarta(null, requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCarta);
    }

    // Rota: PUT http://localhost:4004/catalog/carta/123e4567-e89b-12d3-a456-426614174000
    @PutMapping("/carta/{id}")
    public ResponseEntity<CartaResponseDto> editarCarta(
            @PathVariable UUID id, 
            @Valid @RequestBody CartaRequestDto requestDto,
            @RequestHeader("User-cargo") String cargo) {

        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CartaResponseDto cartaAtualizada = catalogService.salvarOuAtualizarCarta(id, requestDto);
        
        return ResponseEntity.ok(cartaAtualizada);
    
    }

    // GET http://localhost:4004/catalog/carta
    // GET http://localhost:4004/catalog/carta?nome=Dragao
    // GET http://localhost:4004/catalog/carta?raridade=Rara
    @GetMapping("/carta")
    public ResponseEntity<List<CartaResponseDto>> listarOuFiltrarCartas(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String raridade,
            @RequestParam(required = false) String tipo) {
        
        List<CartaResponseDto> cartas = catalogService.filtrarCartas(nome, raridade, tipo);
        
        return ResponseEntity.ok(cartas);
    }

    // Rota: DELETE http://localhost:4004/catalog/carta/123e4567-e89b-12d3-a456-426614174000
    @DeleteMapping("/carta/{cartaId}")
    public ResponseEntity<Void> removerCarta(@PathVariable UUID cartaId, @RequestHeader("User-cargo") String cargo) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catalogService.removerCarta(cartaId);
        
        return ResponseEntity.noContent().build();
    }
}