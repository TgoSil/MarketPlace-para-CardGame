package com.quatro.order_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.quatro.order_service.domain.entity.Auction;

public interface AuctionRepository extends MongoRepository<Auction, UUID> {

    List<Auction> findByIdUser(UUID idUser);

    List<Auction> findByStatusAndExpiraEmBefore(String status, Instant now);
}
