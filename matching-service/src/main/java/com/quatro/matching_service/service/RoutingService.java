package com.quatro.matching_service.service;

import com.quatro.matching_service.domain.redis.AuctionInfo;
import com.quatro.matching_service.domain.redis.BidInfo;
import com.quatro.matching_service.domain.redis.MarketStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final MarketStateService marketStateService;


    public void rotearNovoBid(BidInfo bid) {
        Set<UUID> auctionsDaCarta = marketStateService.getAuctionsForCard(bid.idCarta());
        if (auctionsDaCarta.isEmpty()) {
            log.debug("Nenhum leilão disponível para a carta {}. Robô {} aguardando.", bid.idCarta(), bid.idBid());
            return;
        }

        List<UUID> salasEscolhidas = new ArrayList<>();
        for (UUID auctionId : auctionsDaCarta) {
            AuctionInfo auctionInfo = marketStateService.getAuctionInfo(auctionId);
            if (auctionInfo == null) continue;

            BigDecimal limite = bid.limitePagamento() != null ? bid.limitePagamento() : BigDecimal.ZERO;
            BigDecimal minimo = auctionInfo.precoMinimo() != null ? auctionInfo.precoMinimo() : BigDecimal.ZERO;

            if (limite.compareTo(minimo) < 0) {
                continue;
            }

            BigDecimal lanceInicial = minimo;
            marketStateService.updateBidInAuction(auctionId, bid.idBid(), lanceInicial);
            salasEscolhidas.add(auctionId);

            if (salasEscolhidas.size() >= 3) break;
        }

        if (!salasEscolhidas.isEmpty()) {
            BidInfo bidAtualizada = new BidInfo(
                    bid.idBid(), bid.idComprador(), bid.idCarta(),
                    bid.limitePagamento(), bid.perfilCompra(), bid.expiraEm(), salasEscolhidas
            );
            marketStateService.saveBidInfo(bidAtualizada);
            log.info("Robô {} entrou nas filas: {}", bid.idBid(), salasEscolhidas);
        }
    }
}
