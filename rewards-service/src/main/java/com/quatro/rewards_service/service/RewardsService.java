package com.quatro.rewards_service.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.rewards_service.domain.entity.Reward;
import com.quatro.rewards_service.domain.entity.UserStreak;
import com.quatro.rewards_service.domain.repository.RewardRepository;
import com.quatro.rewards_service.domain.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardsService {

    private final UserStreakRepository userStreakRepository;
    private final RewardRepository rewardRepository;

    // Ordem dos tiers para o upgrade por ciclo
    private static final List<String> ORDEM_TIERS =
            List.of("BASICO", "NORMAL", "ESPECIAL", "EPICO", "MITICO", "LENDARIO");

    private static final int TETO_MOEDAS = 5000;

    /**
     * Atualiza o streak do usuário para o dia de hoje e devolve o estado atualizado.
     * Regras:
     *  - 1º login do usuário: começa dia 1, ciclo 0, streak 1
     *  - Login em dia consecutivo: avança dia_ciclo, e vira o ciclo ao passar do dia 30
     *  - Pulou um dia ou mais: reseta tudo
     *  - Já logou hoje: não muda nada
     */
    @Transactional
    public UserStreak atualizarStreak(UUID idUser) {
        LocalDate hoje = LocalDate.now();

        UserStreak streak = userStreakRepository.findById(idUser)
                .orElse(UserStreak.builder()
                        .idUser(idUser)
                        .diaCiclo(1)
                        .ciclo(0)
                        .streak(1)
                        .dataUltimoLogin(hoje)
                        .build());

        LocalDate ultimoLogin = streak.getDataUltimoLogin();

        if (ultimoLogin != null && !ultimoLogin.equals(hoje)) {
            if (ultimoLogin.equals(hoje.minusDays(1))) {
                streak.setStreak(streak.getStreak() + 1);
                int novoDia = streak.getDiaCiclo() + 1;
                if (novoDia > 30) {
                    novoDia = 1;
                    streak.setCiclo(streak.getCiclo() + 1);
                }
                streak.setDiaCiclo(novoDia);
            } else {
                streak.setStreak(1);
                streak.setDiaCiclo(1);
                streak.setCiclo(0);
            }
            streak.setDataUltimoLogin(hoje);
        }

        return userStreakRepository.save(streak);
    }

    /** Moedas do dia, com multiplicador x13 por ciclo acumulado e teto de 5000 */
    public int calcularMoedas(int moedasBase, int ciclosAcumulados) {
        long valor = moedasBase;
        for (int i = 0; i < ciclosAcumulados; i++) {
            valor *= 13;
            if (valor >= TETO_MOEDAS) return TETO_MOEDAS;
        }
        return (int) Math.min(valor, TETO_MOEDAS);
    }

    /** Tier do dia, subindo 1 nível por ciclo acumulado, com teto em LENDARIO */
    public String calcularTierPacote(String tierBase, int ciclosAcumulados) {
        int indice = ORDEM_TIERS.indexOf(tierBase) + ciclosAcumulados;
        indice = Math.min(indice, ORDEM_TIERS.size() - 1);
        return ORDEM_TIERS.get(indice);
    }

    /** Busca a recompensa configurada para um dia do ciclo (1 a 30) */
    public Reward buscarRecompensaDoDia(int diaCiclo) {
        return rewardRepository.findById(diaCiclo)
                .orElseThrow(() -> new IllegalStateException(
                        "Não há recompensa para o dia " + diaCiclo));
    }
}