package com.quatro.order_service.kafka;

import tools.jackson.databind.ObjectMapper;
import com.quatro.order_service.domain.event.TransacaoResultadoEvent;
import com.quatro.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransacaoResultadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransacaoResultadoConsumer.class);

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @KafkaListener(topics = TransacaoResultadoEvent.TOPICO, groupId = "order-service")
    public void consumir(String payload) {
        try {
            TransacaoResultadoEvent evento = objectMapper.readValue(payload, TransacaoResultadoEvent.class);
            log.info("Evento TRANSACAO_RESULTADO recebido: transacaoId={}, status={}",
                    evento.transacaoId(), evento.status());

            switch (evento.status()) {
                case "CONCLUIDA" -> {
                    orderService.marcarComoConcluido(evento.ordemVendaId(), evento.ordemCompraId());
                    log.info("Auction {} e Bid {} marcadas como CONCLUIDO.", evento.ordemVendaId(), evento.ordemCompraId());
                }
                case "FALHA" -> {
                    orderService.marcarComoFalho(evento.ordemVendaId(), evento.ordemCompraId(), evento.razaoFalha());
                    log.warn("Transação {} falhou. Razão: {}. Auction: {}, Bid: {}",
                            evento.transacaoId(), evento.razaoFalha(), evento.ordemVendaId(), evento.ordemCompraId());
                }
                default -> log.warn("Status desconhecido recebido: {}", evento.status());
            }

        } catch (Exception e) {
            log.error("Erro ao processar evento TRANSACAO_RESULTADO. Payload: {} - Motivo: {}",
                    payload, e.getMessage());
        }
    }
}
