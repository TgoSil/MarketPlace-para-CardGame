package com.quatro.profile_service.grpc;

import com.quatro.grpc.profile.ProfileServiceGrpc;
import com.quatro.grpc.profile.TransfereDinheiroRequest;
import com.quatro.grpc.profile.TransfereDinheiroResponse;
import com.quatro.profile_service.service.ProfileService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ProfileGrpcServer extends ProfileServiceGrpc.ProfileServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ProfileGrpcServer.class);
    private final ProfileService profileService;

    @Override
    public void transfereDinheiro(TransfereDinheiroRequest request, StreamObserver<TransfereDinheiroResponse> responseObserver) {
        log.info("Requisição RPC recebida para transferir dinheiro. Valor: {}", request.getPrecoCarta());
        boolean sucesso;
        String mensagem;

        try {
            profileService.processarTransferencia(
                UUID.fromString(request.getIdUser2()), 
                UUID.fromString(request.getIdUser1()), 
                request.getPrecoCarta()
            );
            
            sucesso = true;
            mensagem = "Transferência monetária concluída.";
            log.info("Transferência de saldo processada com sucesso.");
            
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos na RPC de transferência", e);
            sucesso = false;
            mensagem = e.getMessage();
        } catch (RuntimeException e) {
            log.error("Falha ao conferir/transferir saldo", e);
            sucesso = false;
            mensagem = e.getMessage();
        } catch (Exception e) {
            log.error("Erro interno inesperado ao processar RPC de transferência", e);
            sucesso = false;
            mensagem = "Erro interno no servidor de perfil.";
        }

        TransfereDinheiroResponse response = TransfereDinheiroResponse.newBuilder()
                .setSucesso(sucesso)
                .setMensagem(mensagem)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validaSaldoUsuario(com.quatro.grpc.profile.ValidaSaldoRequest request, StreamObserver<com.quatro.grpc.profile.ValidaSaldoResponse> responseObserver) {
        log.info("Requisição RPC recebida para validar saldo. User: {}, Valor necessário: {}", request.getIdUser(), request.getValorNecessario());
        boolean possui = false;

        try {
            com.quatro.profile_service.domain.dto.CarteiraResponseDto carteira = profileService.buscarCarteira(UUID.fromString(request.getIdUser()));
            if (carteira.getDinheiro() >= request.getValorNecessario()) {
                possui = true;
            } else {
                log.info("Usuário {} possui saldo insuficiente. Saldo: {}, Necessário: {}", request.getIdUser(), carteira.getDinheiro(), request.getValorNecessario());
            }
        } catch (Exception e) {
            log.info("Erro ao buscar carteira do usuário {}: {}", request.getIdUser(), e.getMessage());
        }

        com.quatro.grpc.profile.ValidaSaldoResponse response = com.quatro.grpc.profile.ValidaSaldoResponse.newBuilder()
                .setPossuiSaldo(possui)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}