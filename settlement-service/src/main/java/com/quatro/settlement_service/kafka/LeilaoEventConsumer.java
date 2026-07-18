package com.quatro.settlement_service.kafka;

import tools.jackson.databind.ObjectMapper;
import com.quatro.settlement_service.domain.dto.TransacaoRequestDto;
import com.quatro.settlement_service.domain.event.LeilaoEvent;
import com.quatro.settlement_service.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeilaoEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LeilaoEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final SettlementService transacaoService;

    @KafkaListener(topics = LeilaoEvent.TOPICO, groupId = "settlement-service")
    public void consumir(String payload) {
        try {
            // Desserialização segura
            LeilaoEvent evento = objectMapper.readValue(payload, LeilaoEvent.class);
            log.info("Evento LEILAO_CONCLUIDO recebido: {}", evento);

            // Mapeando o evento para o RequestDto esperado pelo Service
            TransacaoRequestDto request = TransacaoRequestDto.builder()
                    .ordemCompraId(evento.ordemCompraId())
                    .ordemVendaId(evento.ordemVendaId())
                    .compradorId(evento.compradorId())
                    .vendedorId(evento.vendedorId())
                    .cartaId(evento.cartaId())
                    .preco(evento.preco())
                    .quantidade(evento.quantidade())
                    .build();

            // Persiste a transação no banco como INITIATED
            transacaoService.adicionarTransacao(request);
            log.info("Transação registrada no banco e pronta para liquidação.");

        } catch (Exception e) {
            log.error("Erro ao processar o evento LEILAO_CONCLUIDO. Payload: {} - Motivo: {}", payload, e.getMessage());
        }
    }
}