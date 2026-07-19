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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.dto.InventarioResponseDto;
import com.quatro.inventory_service.service.InventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/usuario")
    public ResponseEntity<InventarioResponseDto> adicionarItem(
            @RequestHeader("User-Id") UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody InventarioRequestDto requestDto) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(inventarioService.adicionarOuAtualizarCarta(userId, requestDto));
    }

    @PostMapping("/usuario/{userId}")
    public ResponseEntity<InventarioResponseDto> adicionarItemNoUsuario(
            @PathVariable UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody InventarioRequestDto requestDto) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(inventarioService.adicionarOuAtualizarCarta(userId, requestDto));
    }

    // --- READ ---
    // Buscar todas as cartas de um usuário específico
    @GetMapping("/usuario")
    public ResponseEntity<List<InventarioResponseDto>> buscarInventarioPorUsuario(
            @RequestHeader("User-Id") UUID userId) {
        
        List<InventarioResponseDto> response = inventarioService.buscarInventarioPorUsuario(userId);
        return ResponseEntity.ok(response);
    }

    // Buscar uma carta específica no inventário de um usuário
    @GetMapping("/usuario/carta/{cartaId}")
    public ResponseEntity<InventarioResponseDto> buscarCartaEspecifica(
            @RequestHeader("User-Id") UUID userId, 
            @PathVariable UUID cartaId) {
        
        InventarioResponseDto response = inventarioService.buscarCartaEspecifica(userId, cartaId);
        return ResponseEntity.ok(response);
    }

    // --- DELETE ---
    @DeleteMapping("/usuario")
    public ResponseEntity<InventarioResponseDto> removerCarta(
            @RequestHeader("User-Id") UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody InventarioRequestDto requestDto) {
        
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        InventarioResponseDto resposta = inventarioService.removerCarta(userId, requestDto);
        
        if(resposta!=null){
            return ResponseEntity.ok(resposta);
        }else{
            return ResponseEntity.noContent().build();
        }

    }
}