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

    /**
     * Quando um robô chega no mercado, ele avalia as auctions disponíveis para a sua carta.
     * Ele tenta entrar em até 3 leilões diferentes.
     */
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

            // Se o limite do robô é menor que o preço mínimo, ele nem tenta entrar
            if (bid.limitePagamento().compareTo(auctionInfo.precoMinimo()) < 0) {
                continue;
            }

            // O robô entra na fila cobrindo o preço mínimo inicialmente
            BigDecimal lanceInicial = auctionInfo.precoMinimo();
            marketStateService.updateBidInAuction(auctionId, bid.idBid(), lanceInicial);
            salasEscolhidas.add(auctionId);

            if (salasEscolhidas.size() >= 3) break; // Limite de 3 filas
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
