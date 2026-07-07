package com.quatro.inventory_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.dto.InventarioResponseDto;
import com.quatro.inventory_service.service.InventarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    // --- CREATE / UPDATE ---
    @PostMapping
    public ResponseEntity<InventarioResponseDto> adicionarOuAtualizarCarta(
            @Valid @RequestBody InventarioRequestDto request) {
        
        InventarioResponseDto response = inventarioService.adicionarOuAtualizarCarta(request);
        // Retorna status 201 (Created) quando a operação é bem-sucedida
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- READ ---
    // Buscar todas as cartas de um usuário específico
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<InventarioResponseDto>> buscarInventarioPorUsuario(
            @PathVariable UUID userId) {
        
        List<InventarioResponseDto> response = inventarioService.buscarInventarioPorUsuario(userId);
        return ResponseEntity.ok(response);
    }

    // Buscar uma carta específica no inventário de um usuário
    @GetMapping("/usuario/{userId}/carta/{cartaId}")
    public ResponseEntity<InventarioResponseDto> buscarCartaEspecifica(
            @PathVariable UUID userId, 
            @PathVariable UUID cartaId) {
        
        InventarioResponseDto response = inventarioService.buscarCartaEspecifica(userId, cartaId);
        return ResponseEntity.ok(response);
    }

    // --- DELETE ---
    // Remover uma carta específica de um usuário
    @DeleteMapping("/usuario/{userId}/carta/{cartaId}")
    public ResponseEntity<Void> removerCartaTotalmente(
            @PathVariable UUID userId, 
            @PathVariable UUID cartaId) {
        
        inventarioService.removerCartaTotalmente(userId, cartaId);
        // Retorna status 204 (No Content) indicando que a exclusão ocorreu com sucesso
        return ResponseEntity.noContent().build();
    }

    // Remover todo o inventário de um usuário (Ex: usuário deletou a conta)
    @DeleteMapping("/usuario/{userId}")
    public ResponseEntity<Void> deletarInventarioDoUsuario(
            @PathVariable UUID userId) {
        
        inventarioService.deletarInventarioDoUsuario(userId);
        return ResponseEntity.noContent().build();
    }
}