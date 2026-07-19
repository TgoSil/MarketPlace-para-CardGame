package com.quatro.order_service.grpc;

import com.quatro.grpc.inventory.InventoryServiceGrpc;
import com.quatro.grpc.inventory.ValidaPosseRequest;
import com.quatro.grpc.inventory.ValidaPosseResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryGrpcClient.class);

    @GrpcClient("inventoryService")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public boolean validarPosseCarta(UUID userId, UUID cartaId) {
        log.info("Validando posse da carta {} para o usuário {} via gRPC...", cartaId, userId);
        
        try {
            ValidaPosseRequest request = ValidaPosseRequest.newBuilder()
                    .setIdUser(userId.toString())
                    .setIdCarta(cartaId.toString())
                    .build();

            ValidaPosseResponse response = inventoryStub
                    .withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                    .validaPosseCarta(request);
            
            return response.getPossuiCarta();
        } catch (Exception e) {
            log.error("Erro ao comunicar com inventory-service via gRPC: {}", e.getMessage());
            return false;
        }
    }
}
