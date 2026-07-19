package com.quatro.order_service.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.quatro.order_service.domain.dto.AuctionRequestDto;
import com.quatro.order_service.domain.dto.AuctionResponseDto;
import com.quatro.order_service.domain.dto.BidRequestDto;
import com.quatro.order_service.domain.dto.BidResponseDto;
import com.quatro.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/auction")
    public ResponseEntity<AuctionResponseDto> criarAuction(
            @RequestHeader("User-Id") UUID userId,
            @RequestBody AuctionRequestDto request) {

        AuctionResponseDto response = orderService.createAuction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bid")
    public ResponseEntity<BidResponseDto> criarBid(
            @RequestHeader("User-Id") UUID userId,
            @RequestBody BidRequestDto request) {

        BidResponseDto response = orderService.createBid(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> listarOrdens(
            @RequestHeader("User-Id") UUID userId) {

        Map<String, Object> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/auctions")
    public ResponseEntity<List<AuctionResponseDto>> getAllAuctions() {

        return ResponseEntity.ok(orderService.getAllAuctions());
    }

    @GetMapping("/bids")
    public ResponseEntity<List<BidResponseDto>> getAllBids() {

        return ResponseEntity.ok(orderService.getAllBids());
    }

    @PatchMapping("/orders/{id}/cancel")
    public ResponseEntity<Void> cancelarOrdem(
            @RequestHeader("User-Id") UUID userId,
            @PathVariable UUID id) {

        orderService.cancelOrder(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deletarOrdem(
            @RequestHeader(value = "User-cargo", defaultValue = "") String userCargo,
            @PathVariable UUID id) {
        
        if (!"ADMIN".equalsIgnoreCase(userCargo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}