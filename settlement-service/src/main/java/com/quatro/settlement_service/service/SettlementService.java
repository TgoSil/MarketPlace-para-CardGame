package com.quatro.settlement_service.service;

import com.quatro.settlement_service.domain.dto.TransacaoRequestDto;
import com.quatro.settlement_service.domain.dto.TransacaoResponseDto;
import com.quatro.settlement_service.domain.entity.Transacao;
import com.quatro.settlement_service.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final TransacaoRepository transacaoRepository;

    public TransacaoResponseDto adicionarTransacao(TransacaoRequestDto request) {
        Transacao transacao = Transacao.builder()
                .ordemCompraId(request.getOrdemCompraId())
                .ordemVendaId(request.getOrdemVendaId())
                .compradorId(request.getCompradorId())
                .vendedorId(request.getVendedorId())
                .cartaId(request.getCartaId())
                .preco(request.getPreco())
                .quantidade(request.getQuantidade())
                .status("INITIATED")
                .build();

        Transacao transacaoSalva = transacaoRepository.save(transacao);
        return mapearParaResponse(transacaoSalva);
    }

    public TransacaoResponseDto buscarTransacao(UUID id) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada."));
        
        return mapearParaResponse(transacao);
    }

    public TransacaoResponseDto editarTransacao(UUID id, TransacaoRequestDto request) {
        Transacao transacaoExistente = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada para edição."));

        transacaoExistente.setOrdemCompraId(request.getOrdemCompraId());
        transacaoExistente.setOrdemVendaId(request.getOrdemVendaId());
        transacaoExistente.setCompradorId(request.getCompradorId());
        transacaoExistente.setVendedorId(request.getVendedorId());
        transacaoExistente.setCartaId(request.getCartaId());
        transacaoExistente.setPreco(request.getPreco());
        transacaoExistente.setQuantidade(request.getQuantidade());
        

        Transacao transacaoAtualizada = transacaoRepository.save(transacaoExistente);
        return mapearParaResponse(transacaoAtualizada);
    }

    public void deletarTransacao(UUID id) {
        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada para deleção."));
        
        transacaoRepository.delete(transacao);
    }

    private TransacaoResponseDto mapearParaResponse(Transacao transacao) {
        return TransacaoResponseDto.builder()
                .id(transacao.getId())
                .ordemCompraId(transacao.getOrdemCompraId())
                .ordemVendaId(transacao.getOrdemVendaId())
                .compradorId(transacao.getCompradorId())
                .vendedorId(transacao.getVendedorId())
                .cartaId(transacao.getCartaId())
                .preco(transacao.getPreco())
                .quantidade(transacao.getQuantidade())
                .status(transacao.getStatus())
                .razaoFalha(transacao.getRazaoFalha())
                .criadoEm(transacao.getCriadoEm())
                .atualizadoEm(transacao.getAtualizadoEm())
                .build();
    }
}