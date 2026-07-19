package com.quatro.inventory_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.inventory_service.domain.dto.InventarioRequestDto;
import com.quatro.inventory_service.domain.dto.InventarioResponseDto;
import com.quatro.inventory_service.domain.entity.Carta;
import com.quatro.inventory_service.domain.entity.Inventario;
import com.quatro.inventory_service.domain.entity.UsuarioCartaId;
import com.quatro.inventory_service.repository.CartaRepository;
import com.quatro.inventory_service.repository.InventarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final CartaRepository cartaRepository;

    // --- CREATE / UPDATE ---
    @Transactional
    public InventarioResponseDto adicionarOuAtualizarCarta(UUID userId, InventarioRequestDto request) {
        int quantidadeParaAdicionar = request.getQuantidade() != null ? request.getQuantidade() : 1;

        Optional<Inventario> inventarioExistente = inventarioRepository.findByUserIdAndCartaId(userId, request.getCartaId());

        Inventario inventario;
        if (inventarioExistente.isPresent()) {
            inventario = inventarioExistente.get();
            inventario.setQuantidade(inventario.getQuantidade() + quantidadeParaAdicionar);
        } else {
            Optional<Carta> cartaExiste = cartaRepository.findById(request.getCartaId());
            if(cartaExiste.isPresent()){
                inventario = Inventario.builder()
                    .userId(userId)
                    .cartaId(request.getCartaId())
                    .quantidade(quantidadeParaAdicionar)
                    .carta(cartaExiste.get())
                    .build();
            }else{
                throw new RuntimeException("Carta não existe.");
            }
        }

        Inventario salvo = inventarioRepository.save(inventario);
        return converterParaDto(salvo);
    }

    // --- READ ---
    public List<InventarioResponseDto> buscarInventarioPorUsuario(UUID userId) {
        List<Inventario> inventarioUsuario = inventarioRepository.findAllByUserId(userId);
        
        return inventarioUsuario.stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public InventarioResponseDto buscarCartaEspecifica(UUID userId, UUID cartaId) {
        Inventario inventario = inventarioRepository.findByUserIdAndCartaId(userId, cartaId)
                .orElseThrow(() -> new RuntimeException("Carta não encontrada no inventário do usuário."));
        
        return converterParaDto(inventario);
    }

    // --- DELETE ---
    @Transactional
    public void removerCartaTotalmente(UUID userId, UUID cartaId) {
        UsuarioCartaId id = new UsuarioCartaId(userId, cartaId);
        if (inventarioRepository.existsById(id)) {
            inventarioRepository.deleteById(id);
        } else {
            throw new RuntimeException("Carta não encontrada para remoção.");
        }
    }

    @Transactional
    public InventarioResponseDto removerCarta(UUID usuarioId, InventarioRequestDto request) {
        int quantidadeParaRemover = request.getQuantidade() != null ? request.getQuantidade() : 1;

        Optional<Inventario> inventarioExistente = inventarioRepository.findByUserIdAndCartaId(usuarioId, request.getCartaId());

        Inventario inventario = inventarioExistente.get();;
        
        if(inventario.getQuantidade()==quantidadeParaRemover){
            removerCartaTotalmente(inventario.getUserId(), inventario.getCartaId());
            return null;
        }else{
            inventario.setQuantidade(inventario.getQuantidade() - quantidadeParaRemover);
            Inventario salvo = inventarioRepository.save(inventario);
            return converterParaDto(salvo);
        }
    }

    @Transactional
    public void deletarInventarioDoUsuario(UUID userId) {
        inventarioRepository.deleteAllByUserId(userId);
    }

    // --- TRANSFERÊNCIA (gRPC) ---
    @Transactional
    public void processarTransferencia(UUID vendedorId, UUID compradorId, UUID cartaId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade transferida deve ser maior que zero.");
        }

        // 1. Verifica e remove do vendedor
        Inventario inventarioVendedor = inventarioRepository.findByUserIdAndCartaId(vendedorId, cartaId)
                .orElseThrow(() -> new RuntimeException("O vendedor não possui esta carta no inventário."));

        if (inventarioVendedor.getQuantidade() < quantidade) {
            throw new RuntimeException("O vendedor não possui cartas suficientes para a transferência.");
        }

        if (inventarioVendedor.getQuantidade() == quantidade) {
            // Remove o registro totalmente se ele vender todas as cartas que tem
            removerCartaTotalmente(vendedorId, cartaId);
        } else {
            // Apenas subtrai a quantidade
            inventarioVendedor.setQuantidade(inventarioVendedor.getQuantidade() - quantidade);
            inventarioRepository.save(inventarioVendedor);
        }

        // 2. Adiciona ao comprador (reaproveitando o seu método já existente)
        InventarioRequestDto adicionarRequest = InventarioRequestDto.builder()
                .cartaId(cartaId)
                .quantidade(quantidade)
                .build();
        
        adicionarOuAtualizarCarta(compradorId, adicionarRequest);
    }

    // --- MÉTODOS AUXILIARES ---
    private InventarioResponseDto converterParaDto(Inventario inventario) {
        return InventarioResponseDto.builder()
                .nomeCarta(inventario.getCarta() != null ? inventario.getCarta().getNome() : "Carta Desconhecida")
                .quantidade(inventario.getQuantidade())
                .build();
    }
}