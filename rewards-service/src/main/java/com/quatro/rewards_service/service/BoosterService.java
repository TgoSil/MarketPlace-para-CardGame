package com.quatro.rewards_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.quatro.rewards_service.domain.entity.Carta;
import com.quatro.rewards_service.domain.entity.PacoteProbabilidade;
import com.quatro.rewards_service.domain.repository.CartaRepository;
import com.quatro.rewards_service.domain.repository.PacoteProbabilidadeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoosterService {

    public static final int CARTAS_POR_PACOTE = 5;

    private final PacoteProbabilidadeRepository probabilidadeRepository;
    private final CartaRepository cartaRepository;

    private final Random random = new Random();

    /**
     * Sorteia as 5 cartas de um booster do tier informado.
     * - Cartas podem se repetir
     * - Ignora raridades com peso 0 ou sem nenhuma carta cadastrada
     */
    public List<Carta> abrirBooster(String tierPacote) {
        List<PacoteProbabilidade> probabilidades =
                probabilidadeRepository.findByTierPacote(tierPacote);

        if (probabilidades.isEmpty()) {
            throw new IllegalStateException(
                    "Nenhuma probabilidade cadastrada para o tier " + tierPacote);
        }

        // Monta a "régua": só raridades com peso > 0 E que possuem cartas
        List<String> raridades = new ArrayList<>();
        List<Double> pesos = new ArrayList<>();
        double somaPesos = 0;

        for (PacoteProbabilidade p : probabilidades) {
            double peso = p.getPorcentagem().doubleValue();
            if (peso > 0 && !cartaRepository.findByRaridade(p.getRaridade()).isEmpty()) {
                raridades.add(p.getRaridade());
                pesos.add(peso);
                somaPesos += peso;
            }
        }

        if (raridades.isEmpty()) {
            throw new IllegalStateException(
                    "Não há cartas cadastradas para as raridades do tier " + tierPacote);
        }

        List<Carta> cartasSorteadas = new ArrayList<>();
        for (int i = 0; i < CARTAS_POR_PACOTE; i++) {
            String raridade = sortearRaridade(raridades, pesos, somaPesos);
            cartasSorteadas.add(sortearCartaDaRaridade(raridade));
        }
        return cartasSorteadas;
    }

    /** Caminha pela distribuição acumulada até achar o segmento onde o sorteio caiu. */
    private String sortearRaridade(List<String> raridades, List<Double> pesos, double somaPesos) {
        double sorteio = random.nextDouble() * somaPesos;
        double acumulado = 0;
        for (int i = 0; i < raridades.size(); i++) {
            acumulado += pesos.get(i);
            if (sorteio < acumulado) {
                return raridades.get(i);
            }
        }
        return raridades.get(raridades.size() - 1);
    }

    private Carta sortearCartaDaRaridade(String raridade) {
        List<Carta> cartas = cartaRepository.findByRaridade(raridade);
        return cartas.get(random.nextInt(cartas.size()));
    }
}