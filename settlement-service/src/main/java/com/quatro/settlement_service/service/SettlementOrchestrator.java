package com.quatro.settlement_service.service;

import com.quatro.grpc.inventory.InventoryServiceGrpc;
import com.quatro.grpc.inventory.TransfereCartaRequest;
import com.quatro.grpc.inventory.TransfereCartaResponse;
import com.quatro.grpc.profile.ProfileServiceGrpc;
import com.quatro.grpc.profile.TransfereDinheiroRequest;
import com.quatro.grpc.profile.TransfereDinheiroResponse;
import com.quatro.settlement_service.domain.entity.Transacao;
import com.quatro.settlement_service.domain.event.TransacaoEvent;
import com.quatro.settlement_service.kafka.TransacaoEventProducer;
import com.quatro.settlement_service.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SettlementOrchestrator.class);

    private final TransacaoRepository transacaoRepository;
    private final TransacaoEventProducer transacaoProducer;

    @GrpcClient("inventory-client")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    @GrpcClient("profile-client")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileStub;

    public void processarLiquidacao(Transacao transacao) {
        
        // --- 1. ETAPA DE DINHEIRO (Profile Service) ---
        TransfereDinheiroRequest moneyReq = TransfereDinheiroRequest.newBuilder()
                .setIdUser1(transacao.getVendedorId().toString())
                .setIdUser2(transacao.getCompradorId().toString())
                .setPrecoCarta(transacao.getPreco().doubleValue())
                .build();

        TransfereDinheiroResponse moneyRes = profileStub
                .withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                .transfereDinheiro(moneyReq);
        
        if (!moneyRes.getSucesso()) {
            log.warn("Transação Falhou no Profile Service: {}", moneyRes.getMensagem());
            falharTransacao(transacao, moneyRes.getMensagem());
            return;
        }

        // Salva o checkpoint de que o dinheiro já foi movimentado
        atualizarStatus(transacao, "DINHEIRO_TRANSFERIDO");

        // --- 2. ETAPA DE CARTAS (Inventory Service) ---
        TransfereCartaRequest cardReq = TransfereCartaRequest.newBuilder()
                .setIdCarta(transacao.getCartaId().toString())
                .setIdUser1(transacao.getVendedorId().toString())
                .setIdUser2(transacao.getCompradorId().toString())
                .setQuantidade(transacao.getQuantidade())
                .build();

        TransfereCartaResponse cardRes = inventoryStub
                .withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                .transfereCarta(cardReq);

        if (!cardRes.getSucesso()) {
            log.warn("Transação Falhou no Inventory Service: {}", cardRes.getMensagem());
            
            // ATENÇÃO: Como o dinheiro já foi transferido na etapa 1, 
            // precisamos de uma transação compensatória (Rollback) aqui antes de falhar.
            estornarDinheiro(transacao);
            
            falharTransacao(transacao, "Falha no inventário: " + cardRes.getMensagem() + " (Estorno realizado)");
            return;
        }

        // --- 3. CONCLUSÃO ---
        log.info("Transação {} liquidada com sucesso em ambos os serviços!", transacao.getId());
        atualizarStatus(transacao, TransacaoEvent.STATUS_CONCLUIDA);
        publicarEventoFinal(transacao, TransacaoEvent.STATUS_CONCLUIDA);
    }

    // --- MÉTODOS AUXILIARES ---

    private void atualizarStatus(Transacao transacao, String novoStatus) {
        transacao.setStatus(novoStatus);
        transacaoRepository.save(transacao);
    }

    private void falharTransacao(Transacao transacao, String motivo) {
        transacao.setStatus(TransacaoEvent.STATUS_FALHA);
        transacao.setRazaoFalha(motivo);
        transacaoRepository.save(transacao);
        publicarEventoFinal(transacao, TransacaoEvent.STATUS_FALHA);
    }

    private void publicarEventoFinal(Transacao transacao, String statusFinal) {
        TransacaoEvent evento = new TransacaoEvent(
                transacao.getId(),
                transacao.getOrdemCompraId(),
                transacao.getOrdemVendaId(),
                transacao.getCompradorId(),
                transacao.getVendedorId(),
                transacao.getCartaId(),
                transacao.getPreco(),
                transacao.getQuantidade(),
                statusFinal,
                transacao.getRazaoFalha()
        );
        transacaoProducer.publicarResultadoTransacao(evento);
    }

    private void estornarDinheiro(Transacao transacao) {
        log.info("Iniciando estorno para a transação {}. Devolvendo fundos ao comprador.", transacao.getId());
        
        // Invertendo os papéis para o estorno: 
        // idUser2 (Origem) passa a ser o Vendedor, que devolve o dinheiro.
        // idUser1 (Destino) passa a ser o Comprador, que recebe de volta.
        TransfereDinheiroRequest rollbackReq = TransfereDinheiroRequest.newBuilder()
                .setIdUser1(transacao.getCompradorId().toString()) 
                .setIdUser2(transacao.getVendedorId().toString())  
                .setPrecoCarta(transacao.getPreco().doubleValue())
                .build();

        try {
            TransfereDinheiroResponse rollbackRes = profileStub
                    .withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                    .transfereDinheiro(rollbackReq);
            
            if (rollbackRes.getSucesso()) {
                log.info("Estorno contábil realizado com sucesso para a transação {}", transacao.getId());
            } else {
                // Falha lógica (ex: o vendedor não tinha mais saldo para devolver)
                log.error("FALHA CRÍTICA NO ESTORNO da transação {}: {}", transacao.getId(), rollbackRes.getMensagem());
                atualizarStatus(transacao, "ROLLBACK_FAILED");
            }
        } catch (Exception e) {
            // Falha de infraestrutura (ex: o profile-service caiu durante o estorno)
            log.error("Erro de comunicação RPC ao tentar realizar o estorno da transação {}", transacao.getId(), e);
            atualizarStatus(transacao, "ROLLBACK_FAILED");
        }
    }
}