package com.quatro.rewards_service.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.quatro.rewards_service.domain.dto.ResgateResponseDto;
import com.quatro.rewards_service.domain.dto.StatusRecompensaDto;
import com.quatro.rewards_service.service.RewardsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    @GetMapping("/status")
    public ResponseEntity<StatusRecompensaDto> consultarStatus(@RequestHeader("User-Id") UUID userId) {
        return ResponseEntity.ok(rewardsService.consultarStatus(userId));
    }

    @PostMapping("/resgate")
    public ResponseEntity<ResgateResponseDto> resgatar(@RequestHeader("User-Id") UUID userId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rewardsService.resgatar(userId));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}