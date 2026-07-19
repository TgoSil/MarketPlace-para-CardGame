package com.quatro.rewards_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quatro.rewards_service.domain.entity.Carta;
import com.quatro.rewards_service.domain.entity.PacoteProbabilidade;
import com.quatro.rewards_service.domain.repository.CartaRepository;
import com.quatro.rewards_service.domain.repository.PacoteProbabilidadeRepository;
import com.quatro.rewards_service.service.BoosterService;

class BoosterServiceTest {

    private PacoteProbabilidadeRepository probRepo;
    private CartaRepository cartaRepo;
    private BoosterService service;

    @BeforeEach
    void setup() {
        probRepo = mock(PacoteProbabilidadeRepository.class);
        cartaRepo = mock(CartaRepository.class);
        service = new BoosterService(probRepo, cartaRepo);

        when(probRepo.findByTierPacote("NORMAL")).thenReturn(List.of(
                prob("NORMAL", "C", 50), prob("NORMAL", "B", 35),
                prob("NORMAL", "A", 10), prob("NORMAL", "S", 4),
                prob("NORMAL", "P", 1)));

        for (String r : List.of("C", "B", "A", "S", "P")) {
            when(cartaRepo.findByRaridade(r)).thenReturn(
                    List.of(new Carta(UUID.randomUUID(), "Carta " + r, r)));
        }
    }

    @Test
    void boosterSempreTem5Cartas() {
        assertEquals(5, service.abrirBooster("NORMAL").size());
    }

    @Test
    void distribuicaoConvergeParaAsPorcentagens() {
        Map<String, Integer> contagem = new HashMap<>();
        int sorteios = 10_000;

        for (int i = 0; i < sorteios / 5; i++) {
            for (Carta c : service.abrirBooster("NORMAL")) {
                contagem.merge(c.getRaridade(), 1, Integer::sum);
            }
        }

        assertEquals(50.0, 100.0 * contagem.getOrDefault("C", 0) / sorteios, 2.0);
        assertEquals(35.0, 100.0 * contagem.getOrDefault("B", 0) / sorteios, 2.0);
        assertEquals(10.0, 100.0 * contagem.getOrDefault("A", 0) / sorteios, 2.0);
        assertEquals(4.0,  100.0 * contagem.getOrDefault("S", 0) / sorteios, 1.0);
        assertEquals(1.0,  100.0 * contagem.getOrDefault("P", 0) / sorteios, 1.0);
    }

    @Test
    void raridadeSemCartaNaoQuebraOSorteio() {
        when(cartaRepo.findByRaridade("P")).thenReturn(List.of());
        List<Carta> cartas = service.abrirBooster("NORMAL");
        assertEquals(5, cartas.size());
        assertTrue(cartas.stream().noneMatch(c -> c.getRaridade().equals("P")));
    }

    private PacoteProbabilidade prob(String tier, String raridade, double pct) {
        return new PacoteProbabilidade(tier, raridade, BigDecimal.valueOf(pct));
    }
}