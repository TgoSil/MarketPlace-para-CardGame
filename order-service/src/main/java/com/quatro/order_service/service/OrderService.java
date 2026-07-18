package com.quatro.order_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.quatro.order_service.domain.dto.AuctionRequestDto;
import com.quatro.order_service.domain.dto.AuctionResponseDto;
import com.quatro.order_service.domain.dto.BidRequestDto;
import com.quatro.order_service.domain.dto.BidResponseDto;
import com.quatro.order_service.domain.entity.Auction;
import com.quatro.order_service.domain.entity.Bid;
import com.quatro.order_service.domain.event.IntencaoCanceladaEvent;
import com.quatro.order_service.domain.event.IntencaoLeilaoEvent;
import com.quatro.order_service.kafka.OrderEventPublisher;
import com.quatro.order_service.repository.AuctionRepository;
import com.quatro.order_service.repository.BidRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final OrderEventPublisher eventPublisher;

    // ========================
    //  CRIAÇÃO
    // ========================

    public AuctionResponseDto createAuction(UUID userId, AuctionRequestDto request) {
        // TODO (gRPC): Validar se usuário possui a carta antes de criar Auction
        // boolean possuiCarta = inventoryGrpcClient.validarPosseCarta(userId, request.getIdCarta());
        // if (!possuiCarta) {
        //     throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Usuário não possui esta carta.");
        // }

        Auction auction = Auction.builder()
                .idAuction(UUID.randomUUID())
                .idCarta(request.getIdCarta())
                .idUser(userId)
                .precoMinimo(request.getPrecoMinimo())
                .precoTeto(request.getPrecoTeto())
                .criadoEm(Instant.now())
                .expiraEm(request.getExpiraEm())
                .status("ATIVO")
                .build();

        Auction salvo = auctionRepository.save(auction);
        log.info("Auction criada: {} para carta {} pelo usuário {}", salvo.getIdAuction(), salvo.getIdCarta(), userId);

        eventPublisher.publicarIntencaoLeilao(new IntencaoLeilaoEvent(
                "AUCTION",
                salvo.getIdAuction(),
                salvo.getIdCarta(),
                salvo.getIdUser(),
                salvo.getPrecoMinimo(),
                salvo.getPrecoTeto(),
                null,   // limitePagamento não se aplica a Auction
                null,   // perfilCompra não se aplica a Auction
                salvo.getExpiraEm()
        ));

        return mapAuctionToResponse(salvo);
    }

    public BidResponseDto createBid(UUID userId, BidRequestDto request) {
        // TODO (gRPC): Validar se usuário possui saldo suficiente antes de criar Bid
        // boolean possuiSaldo = profileGrpcClient.validarSaldoUsuario(userId, request.getLimitePagamento());
        // if (!possuiSaldo) {
        //     throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficiente.");
        // }

        Bid bid = Bid.builder()
                .id(UUID.randomUUID())
                .idCarta(request.getIdCarta())
                .idUser(userId)
                .limitePagamento(request.getLimitePagamento())
                .perfilCompra(request.getPerfilCompra())
                .criadoEm(Instant.now())
                .expiraEm(request.getExpiraEm())
                .status("ATIVO")
                .build();

        Bid salvo = bidRepository.save(bid);
        log.info("Bid criada: {} para carta {} pelo usuário {}", salvo.getId(), salvo.getIdCarta(), userId);

        eventPublisher.publicarIntencaoLeilao(new IntencaoLeilaoEvent(
                "BID",
                salvo.getId(),
                salvo.getIdCarta(),
                salvo.getIdUser(),
                null,   // precoMinimo não se aplica a Bid
                null,   // precoTeto não se aplica a Bid
                salvo.getLimitePagamento(),
                salvo.getPerfilCompra(),
                salvo.getExpiraEm()
        ));

        return mapBidToResponse(salvo);
    }

    // ========================
    //  CONSULTA
    // ========================

    public Map<String, Object> getOrdersByUser(UUID userId) {
        List<AuctionResponseDto> auctions = auctionRepository.findByIdUser(userId)
                .stream().map(this::mapAuctionToResponse).toList();

        List<BidResponseDto> bids = bidRepository.findByIdUser(userId)
                .stream().map(this::mapBidToResponse).toList();

        return Map.of("auctions", auctions, "bids", bids);
    }

    public List<AuctionResponseDto> getAllAuctions() {
        return auctionRepository.findAll()
                .stream().map(this::mapAuctionToResponse).toList();
    }

    public List<BidResponseDto> getAllBids() {
        return bidRepository.findAll()
                .stream().map(this::mapBidToResponse).toList();
    }

    // ========================
    //  CANCELAMENTO (iniciado pelo usuário)
    // ========================

    public void cancelOrder(UUID orderId, UUID userId) {
        // Tenta encontrar como Auction
        var auctionOpt = auctionRepository.findById(orderId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            validarDono(auction.getIdUser(), userId);
            validarStatusCancelavel(auction.getStatus());

            auction.setStatus("CANCELADA");
            auctionRepository.save(auction);
            log.info("Auction {} cancelada pelo usuário {}", orderId, userId);

            eventPublisher.publicarIntencaoCancelada(new IntencaoCanceladaEvent(
                    "AUCTION", orderId, auction.getIdCarta(), userId, "CANCELADA_USUARIO"
            ));
            return;
        }

        // Tenta encontrar como Bid
        var bidOpt = bidRepository.findById(orderId);
        if (bidOpt.isPresent()) {
            Bid bid = bidOpt.get();
            validarDono(bid.getIdUser(), userId);
            validarStatusCancelavel(bid.getStatus());

            bid.setStatus("CANCELADA");
            bidRepository.save(bid);
            log.info("Bid {} cancelada pelo usuário {}", orderId, userId);

            eventPublisher.publicarIntencaoCancelada(new IntencaoCanceladaEvent(
                    "BID", orderId, bid.getIdCarta(), userId, "CANCELADA_USUARIO"
            ));
            return;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem não encontrada.");
    }

    // ========================
    //  ATUALIZAÇÃO DE STATUS (via Kafka - Matching / Settlement)
    // ========================

    public void marcarComoPendente(UUID auctionId, UUID bidId) {
        auctionRepository.findById(auctionId).ifPresent(a -> {
            a.setStatus("PENDENTE");
            auctionRepository.save(a);
        });
        bidRepository.findById(bidId).ifPresent(b -> {
            b.setStatus("PENDENTE");
            bidRepository.save(b);
        });
    }

    public void marcarComoConcluido(UUID auctionId, UUID bidId) {
        auctionRepository.findById(auctionId).ifPresent(a -> {
            a.setStatus("CONCLUIDO");
            auctionRepository.save(a);
        });
        bidRepository.findById(bidId).ifPresent(b -> {
            b.setStatus("CONCLUIDO");
            bidRepository.save(b);
        });
    }

    public void marcarComoFalho(UUID auctionId, UUID bidId, String razaoFalha) {
        // O settlement reporta a falha com uma razão descritiva.
        // Por ora, ambas as ordens voltam a ATIVO para serem re-casadas,
        // já que a transação falhou mas as intenções originais podem ainda ser válidas.
        // TODO: Refinar quando o gRPC de validação estiver implementado — se a razão
        //       indicar que um lado específico é inválido, apenas esse lado deve ser cancelado.
        auctionRepository.findById(auctionId).ifPresent(a -> {
            a.setStatus("ATIVO");
            auctionRepository.save(a);
            log.warn("Auction {} reativada após falha na transação. Razão: {}", auctionId, razaoFalha);
        });
        bidRepository.findById(bidId).ifPresent(b -> {
            b.setStatus("ATIVO");
            bidRepository.save(b);
            log.warn("Bid {} reativada após falha na transação. Razão: {}", bidId, razaoFalha);
        });
    }

    // ========================
    //  EXPIRAÇÃO (chamada pelo Scheduler)
    // ========================

    public void marcarComoCanceladaExterna(UUID orderId, String razao) {
        log.info("Recebido cancelamento externo para ordem {}. Razão: {}", orderId, razao);
        
        // Tenta como Auction
        auctionRepository.findById(orderId).ifPresent(a -> {
            if ("ATIVO".equals(a.getStatus())) {
                a.setStatus(razao); // "EXPIRADA" ou "CANCELADA"
                auctionRepository.save(a);
                log.info("Auction {} marcada como {}", orderId, razao);
            }
        });

        // Tenta como Bid
        bidRepository.findById(orderId).ifPresent(b -> {
            if ("ATIVO".equals(b.getStatus())) {
                b.setStatus(razao); // "EXPIRADA" ou "CANCELADA"
                bidRepository.save(b);
                log.info("Bid {} marcada como {}", orderId, razao);
            }
        });
    }

    // ========================
    //  REMOÇÃO FÍSICA (administrativo)
    // ========================

    public void deleteOrder(UUID orderId) {
        if (auctionRepository.existsById(orderId)) {
            auctionRepository.deleteById(orderId);
            log.info("Auction {} removida fisicamente.", orderId);
            return;
        }
        if (bidRepository.existsById(orderId)) {
            bidRepository.deleteById(orderId);
            log.info("Bid {} removida fisicamente.", orderId);
            return;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem não encontrada para remoção.");
    }

    // ========================
    //  HELPERS
    // ========================

    private void validarDono(UUID donoOrdem, UUID solicitante) {
        if (!donoOrdem.equals(solicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para cancelar esta ordem.");
        }
    }

    private void validarStatusCancelavel(String status) {
        if (!"ATIVO".equals(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só é possível cancelar ordens com status ATIVO. Status atual: " + status);
        }
    }

    private AuctionResponseDto mapAuctionToResponse(Auction auction) {
        return AuctionResponseDto.builder()
                .idAuction(auction.getIdAuction())
                .idCarta(auction.getIdCarta())
                .idUser(auction.getIdUser())
                .precoMinimo(auction.getPrecoMinimo())
                .precoTeto(auction.getPrecoTeto())
                .criadoEm(auction.getCriadoEm())
                .expiraEm(auction.getExpiraEm())
                .status(auction.getStatus())
                .build();
    }

    private BidResponseDto mapBidToResponse(Bid bid) {
        return BidResponseDto.builder()
                .id(bid.getId())
                .idCarta(bid.getIdCarta())
                .idUser(bid.getIdUser())
                .limitePagamento(bid.getLimitePagamento())
                .perfilCompra(bid.getPerfilCompra())
                .criadoEm(bid.getCriadoEm())
                .expiraEm(bid.getExpiraEm())
                .status(bid.getStatus())
                .build();
    }
}
