package com.quatro.order_service.grpc;

import com.quatro.grpc.profile.ProfileServiceGrpc;
import com.quatro.grpc.profile.ValidaSaldoRequest;
import com.quatro.grpc.profile.ValidaSaldoResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProfileGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(ProfileGrpcClient.class);

    @GrpcClient("profileService")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileStub;

    public boolean validarSaldoUsuario(UUID userId, BigDecimal limitePagamento) {
        log.info("Validando saldo necessário de {} para o usuário {} via gRPC...", limitePagamento, userId);
        
        try {
            ValidaSaldoRequest request = ValidaSaldoRequest.newBuilder()
                    .setIdUser(userId.toString())
                    .setValorNecessario(limitePagamento.doubleValue())
                    .build();

            ValidaSaldoResponse response = profileStub
                    .withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS)
                    .validaSaldoUsuario(request);
            
            return response.getPossuiSaldo();
        } catch (Exception e) {
            log.error("Erro ao comunicar com profile-service via gRPC: {}", e.getMessage());
            return false;
        }
    }
}
