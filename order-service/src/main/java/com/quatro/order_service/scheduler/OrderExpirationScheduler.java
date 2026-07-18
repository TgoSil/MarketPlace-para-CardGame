package com.quatro.order_service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.quatro.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

/**
 * Job agendado que verifica periodicamente se há ordens expiradas
 * e as marca como EXPIRADA, avisando o Matching Service via Kafka.
 */
@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    private final OrderService orderService;

    @Scheduled(fixedRate = 300_000) // A cada 5 minutos (300.000 ms)
    public void checarExpiracao() {
        log.debug("Iniciando checagem de ordens expiradas...");
        orderService.verificarOrdensExpiradas();
    }
}
