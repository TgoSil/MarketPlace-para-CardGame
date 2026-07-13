package com.quatro.inventory_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.dto.InventarioResponseDto;
import com.quatro.inventory_service.domain.entity.Inventario;
import com.quatro.inventory_service.domain.entity.UsuarioCartaId;
import com.quatro.inventory_service.repository.InventarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository repository;

    // --- CREATE / UPDATE ---
    @Transactional
    public InventarioResponseDto adicionarOuAtualizarCarta(UUID userId, InventarioRequestDto request) {
        int quantidadeParaAdicionar = request.getQuantidade() != null ? request.getQuantidade() : 1;

        Optional<Inventario> inventarioExistente = repository.findByUserIdAndCartaId(userId, request.getCartaId());

        Inventario inventario;
        if (inventarioExistente.isPresent()) {
            // Se a carta já existe no inventário, soma a quantidade
            inventario = inventarioExistente.get();
            inventario.setQuantidade(inventario.getQuantidade() + quantidadeParaAdicionar);
        } else {
            // Se não existe, cria um novo registro
            inventario = Inventario.builder()
                    .userId(userId)
                    .cartaId(request.getCartaId())
                    .quantidade(quantidadeParaAdicionar)
                    .build();
        }

        Inventario salvo = repository.save(inventario);
        return converterParaDto(salvo);
    }

    // --- READ ---
    public List<InventarioResponseDto> buscarInventarioPorUsuario(UUID userId) {
        List<Inventario> inventarioUsuario = repository.findAllByUserId(userId);
        
        return inventarioUsuario.stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public InventarioResponseDto buscarCartaEspecifica(UUID userId, UUID cartaId) {
        Inventario inventario = repository.findByUserIdAndCartaId(userId, cartaId)
                .orElseThrow(() -> new RuntimeException("Carta não encontrada no inventário do usuário."));
        
        return converterParaDto(inventario);
    }

    // --- DELETE ---
    @Transactional
    public void removerCartaTotalmente(UUID userId, UUID cartaId) {
        UsuarioCartaId id = new UsuarioCartaId(userId, cartaId);
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new RuntimeException("Carta não encontrada para remoção.");
        }
    }

    @Transactional
    public InventarioResponseDto removerCarta(UUID usuarioId, InventarioRequestDto request) {
        int quantidadeParaRemover = request.getQuantidade() != null ? request.getQuantidade() : 1;

        Optional<Inventario> inventarioExistente = repository.findByUserIdAndCartaId(usuarioId, request.getCartaId());

        Inventario inventario = inventarioExistente.get();;
        
        if(inventario.getQuantidade()==quantidadeParaRemover){
            removerCartaTotalmente(inventario.getUserId(), inventario.getCartaId());
            return null;
        }else{
            inventario.setQuantidade(inventario.getQuantidade() - quantidadeParaRemover);
            Inventario salvo = repository.save(inventario);
            return converterParaDto(salvo);
        }
    }

    @Transactional
    public void deletarInventarioDoUsuario(UUID userId) {
        repository.deleteAllByUserId(userId);
    }

    // --- MÉTODOS AUXILIARES ---
    private InventarioResponseDto converterParaDto(Inventario inventario) {
        return InventarioResponseDto.builder()
                .cartaId(inventario.getCartaId())
                .quantidade(inventario.getQuantidade())
                .build();
    }
}