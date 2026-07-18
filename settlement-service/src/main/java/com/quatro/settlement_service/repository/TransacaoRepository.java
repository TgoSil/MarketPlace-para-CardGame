package com.quatro.settlement_service.repository;

import com.quatro.settlement_service.domain.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    /**
     * Busca todas as transações que estão presas em um determinado status.
     * Extremamente útil para recuperar transações "INITIATED" caso o servidor reinicie.
     */
    List<Transacao> findByStatus(String status);

    /**
     * Busca o histórico de compras de um usuário específico.
     */
    List<Transacao> findByCompradorId(UUID compradorId);

    /**
     * Busca o histórico de vendas de um usuário específico.
     */
    List<Transacao> findByVendedorId(UUID vendedorId);
    
    /**
     * Busca transações de uma ordem de compra específica.
     */
    List<Transacao> findByOrdemCompraId(UUID ordemCompraId);
}