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
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    @GetMapping("/status/{userId}")
    public ResponseEntity<StatusRecompensaDto> consultarStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(rewardsService.consultarStatus(userId));
    }

    @PostMapping("/resgate/{userId}")
    public ResponseEntity<ResgateResponseDto> resgatar(@PathVariable UUID userId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rewardsService.resgatar(userId));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}