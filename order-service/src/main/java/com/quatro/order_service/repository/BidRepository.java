package com.quatro.order_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.quatro.order_service.domain.entity.Bid;

public interface BidRepository extends MongoRepository<Bid, UUID> {

    List<Bid> findByIdUser(UUID idUser);

    List<Bid> findByStatusAndExpiraEmBefore(String status, Instant now);
}
