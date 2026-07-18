package com.quatro.order_service.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bids")
public class Bid {

    @Id
    private UUID id;
    private UUID idCarta;
    private UUID idUser;
    
    private BigDecimal limitePagamento;
    private String perfilCompra;
    
    private Instant criadoEm;
    private Instant expiraEm;

    private String status;

}

