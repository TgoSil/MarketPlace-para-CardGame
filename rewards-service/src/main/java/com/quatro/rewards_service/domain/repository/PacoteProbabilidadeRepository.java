package com.quatro.rewards_service.domain.repository;

import com.quatro.rewards_service.domain.entity.PacoteProbabilidade;
import com.quatro.rewards_service.domain.entity.PacoteProbabilidadeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PacoteProbabilidadeRepository extends JpaRepository<PacoteProbabilidade, PacoteProbabilidadeId> {
    List<PacoteProbabilidade> findByTierPacote(String tierPacote);
}