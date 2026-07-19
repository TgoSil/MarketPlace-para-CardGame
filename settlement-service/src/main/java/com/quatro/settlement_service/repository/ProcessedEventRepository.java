package com.quatro.settlement_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quatro.settlement_service.domain.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
