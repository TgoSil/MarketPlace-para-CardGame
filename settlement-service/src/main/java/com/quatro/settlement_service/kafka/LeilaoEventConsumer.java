package com.quatro.settlement_service.kafka;

import java.time.LocalDateTime;

import tools.jackson.databind.ObjectMapper;
import com.quatro.settlement_service.domain.dto.TransacaoRequestDto;
import com.quatro.settlement_service.domain.dto.TransacaoResponseDto;
import com.quatro.settlement_service.domain.entity.ProcessedEvent;
import com.quatro.settlement_service.domain.entity.Transacao;
import com.quatro.settlement_service.domain.event.LeilaoEvent;
import com.quatro.settlement_service.repository.ProcessedEventRepository;
import com.quatro.settlement_service.service.SettlementOrchestrator;
import com.quatro.settlement_service.service.SettlementService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeilaoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LeilaoEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final SettlementService transacaoService;
    private final SettlementOrchestrator settlementOrchestrator;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = LeilaoEvent.TOPICO, groupId = "settlement-service")
    public void consumir(String payload) {
        try {
            LeilaoEvent evento = objectMapper.readValue(payload, LeilaoEvent.class);
            log.info("Evento LEILAO_CONCLUIDO recebido: {}", evento);

            if (processedEventRepository.existsById(evento.idLeilao())) {
                log.info("Leilão {} já processado, ignorando duplicata.", evento.idLeilao());
                return;
            }

            TransacaoRequestDto request = TransacaoRequestDto.builder()
                    .ordemCompraId(evento.idBidVencedora())
                    .ordemVendaId(evento.idLeilao())
                    .compradorId(evento.idComprador())
                    .vendedorId(evento.idVendedor())
                    .cartaId(evento.idCarta())
                    .preco(evento.valorFechamento() != null ? evento.valorFechamento().intValue() : 0)
                    .quantidade(1)
                    .build();

            TransacaoResponseDto transacaoSalva = transacaoService.adicionarTransacao(request);
            log.info("Transação registrada no banco e pronta para liquidação.");

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(evento.idLeilao())
                    .tipo("LEILAO_CONCLUIDO")
                    .processadoEm(LocalDateTime.now())
                    .build());

            Transacao transacao = Transacao.builder()
                .id(transacaoSalva.getId())
                .ordemCompraId(transacaoSalva.getOrdemCompraId())
                .ordemVendaId(transacaoSalva.getOrdemVendaId())
                .compradorId(transacaoSalva.getCompradorId())
                .vendedorId(transacaoSalva.getVendedorId())
                .cartaId(transacaoSalva.getCartaId())
                .preco(transacaoSalva.getPreco())
                .quantidade(transacaoSalva.getQuantidade())
                .status(transacaoSalva.getStatus())
                .razaoFalha(transacaoSalva.getRazaoFalha())
                .criadoEm(transacaoSalva.getCriadoEm())
                .atualizadoEm(transacaoSalva.getAtualizadoEm())
                .build();

            settlementOrchestrator.processarLiquidacao(transacao);

        } catch (Exception e) {
            log.error("Erro ao processar o evento LEILAO_CONCLUIDO. Payload: {} - Motivo: {}", payload, e.getMessage());
        }
    }
}