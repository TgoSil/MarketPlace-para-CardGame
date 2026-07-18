package com.quatro.settlement_service.kafka;

import tools.jackson.databind.ObjectMapper;
import com.quatro.settlement_service.domain.event.TransacaoEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransacaoEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TransacaoEventProducer.class);

    // O template enviará a chave como String (ID da transação) e o valor como String (JSON)
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publicarResultadoTransacao(TransacaoEvent evento) {
        try {
            String payload = objectMapper.writeValueAsString(evento);
            
            // Publica no tópico usando o ID da transação como chave para garantir ordenação nas partições
            kafkaTemplate.send(TransacaoEvent.TOPICO, evento.transacaoId().toString(), payload);
            
            log.info("Evento de resultado de transação publicado com sucesso: {}", payload);
        } catch (Exception e) {
            log.error("Erro ao serializar o evento TransacaoEvent para JSON", e);
        }
    }
}