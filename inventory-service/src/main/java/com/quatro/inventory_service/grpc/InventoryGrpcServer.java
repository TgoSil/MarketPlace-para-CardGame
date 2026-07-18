package com.quatro.inventory_service.grpc;

import com.quatro.grpc.inventory.InventoryServiceGrpc;
import com.quatro.grpc.inventory.TransfereCartaRequest;
import com.quatro.grpc.inventory.TransfereCartaResponse;
import com.quatro.inventory_service.service.InventarioService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcServer extends InventoryServiceGrpc.InventoryServiceImplBase {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryGrpcServer.class);
    private final InventarioService inventarioService;

    @Override
    public void transfereCarta(TransfereCartaRequest request, StreamObserver<TransfereCartaResponse> responseObserver) {
        log.info("Requisição RPC recebida para transferir carta: {}", request.getIdCarta());
        boolean sucesso;
        String mensagem;

        try {
            // Repassando os IDs do Vendedor (User1), Comprador (User2), Carta e Quantidade
            inventarioService.processarTransferencia(
                UUID.fromString(request.getIdUser1()), 
                UUID.fromString(request.getIdUser2()), 
                UUID.fromString(request.getIdCarta()),
                request.getQuantidade()
            );
            
            sucesso = true;
            mensagem = "Transferência de inventário concluída.";
            log.info("Transferência de inventário processada com sucesso.");
            
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos na RPC de transferência de cartas", e);
            sucesso = false;
            mensagem = e.getMessage();
        } catch (RuntimeException e) {
            log.error("Falha ao conferir/transferir carta", e);
            sucesso = false;
            mensagem = e.getMessage(); // Ex: "O vendedor não possui cartas suficientes..."
        } catch (Exception e) {
            log.error("Erro interno inesperado ao processar RPC de transferência de carta", e);
            sucesso = false;
            mensagem = "Erro interno no servidor de inventário.";
        }

        TransfereCartaResponse response = TransfereCartaResponse.newBuilder()
                .setSucesso(sucesso)
                .setMensagem(mensagem)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}