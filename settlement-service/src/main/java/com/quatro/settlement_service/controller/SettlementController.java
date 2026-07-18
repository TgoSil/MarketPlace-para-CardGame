package com.quatro.settlement_service.controller;

import com.quatro.settlement_service.domain.dto.TransacaoRequestDto;
import com.quatro.settlement_service.domain.dto.TransacaoResponseDto;
import com.quatro.settlement_service.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService transacaoService;

    @PostMapping("/transacao")
    public ResponseEntity<TransacaoResponseDto> adicionarTransacao(
            @Valid @RequestBody TransacaoRequestDto request,
            @RequestHeader("User-cargo") String cargo){
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        TransacaoResponseDto response = transacaoService.adicionarTransacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transacao/{id}")
    public ResponseEntity<TransacaoResponseDto> buscarTransacao(@PathVariable UUID id) {
        TransacaoResponseDto response = transacaoService.buscarTransacao(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/transacao/{id}")
    public ResponseEntity<TransacaoResponseDto> editarTransacao(
            @PathVariable UUID id, 
            @Valid @RequestBody TransacaoRequestDto request,
            @RequestHeader("User-cargo") String cargo) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        TransacaoResponseDto response = transacaoService.editarTransacao(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transacao/{id}")
    public ResponseEntity<Void> deletarTransacao(
            @PathVariable UUID id,
            @RequestHeader("User-cargo") String cargo) {
        if(!cargo.equals("ADMIN")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        transacaoService.deletarTransacao(id);
        return ResponseEntity.noContent().build();
    }
}