package com.quatro.rewards_service.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.rewards_service.domain.dto.CartaDto;
import com.quatro.rewards_service.domain.dto.ResgateResponseDto;
import com.quatro.rewards_service.domain.dto.StatusRecompensaDto;
import com.quatro.rewards_service.domain.entity.Carta;
import com.quatro.rewards_service.domain.entity.CartaRecebida;
import com.quatro.rewards_service.domain.entity.Login;
import com.quatro.rewards_service.domain.entity.Reward;
import com.quatro.rewards_service.domain.entity.UserStreak;
import com.quatro.rewards_service.domain.event.RecompensaCartaEvent;
import com.quatro.rewards_service.domain.event.RecompensaDinheiroEvent;
import com.quatro.rewards_service.domain.repository.CartaRecebidaRepository;
import com.quatro.rewards_service.domain.repository.LoginRepository;
import com.quatro.rewards_service.domain.repository.RewardRepository;
import com.quatro.rewards_service.domain.repository.UserStreakRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardsService {

    private final UserStreakRepository userStreakRepository;
    private final RewardRepository rewardRepository;
    private final LoginRepository loginRepository;
    private final CartaRecebidaRepository cartaRecebidaRepository;
    private final BoosterService boosterService;
    private final RewardsEventPublisher eventPublisher;

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

    /** Tier do dia, subindo 1 nível por ciclo acumulado */
    public String calcularTierPacote(String tierBase, int ciclosAcumulados) {
        int indice = ORDEM_TIERS.indexOf(tierBase) + ciclosAcumulados;
        indice = Math.min(indice, ORDEM_TIERS.size() - 1);
        return ORDEM_TIERS.get(indice);
    }

    /** Busca a recompensa configurada para um dia do ciclo  */
    public Reward buscarRecompensaDoDia(int diaCiclo) {
        return rewardRepository.findById(diaCiclo)
                .orElseThrow(() -> new IllegalStateException(
                        "Não há recompensa para o dia " + diaCiclo));
    }

    /** Consulta o que o usuário receberia hoje, sem alterar nada. */
    public StatusRecompensaDto consultarStatus(UUID idUser) {
        LocalDate hoje = LocalDate.now();
        boolean jaResgatouHoje = loginRepository.existsByIdUserAndDataLogin(idUser, hoje);
        UserStreak atual = userStreakRepository.findById(idUser).orElse(null);
        int diaCiclo = 1, ciclo = 0, streak = 1;
        if (atual != null) {
            LocalDate ultimo = atual.getDataUltimoLogin();
            if (ultimo != null && ultimo.equals(hoje)) {
                diaCiclo = atual.getDiaCiclo();
                ciclo = atual.getCiclo();
                streak = atual.getStreak();
            } else if (ultimo != null && ultimo.equals(hoje.minusDays(1))) {
                streak = atual.getStreak() + 1;
                diaCiclo = atual.getDiaCiclo() + 1;
                ciclo = atual.getCiclo();
                if (diaCiclo > 30) {
                    diaCiclo = 1;
                    ciclo++;
                }
            }
        }

        Reward reward = buscarRecompensaDoDia(diaCiclo);
        boolean ehMoedas = "MOEDAS".equals(reward.getTipoReward());

        return StatusRecompensaDto.builder()
                .disponivel(!jaResgatouHoje)
                .streakAtual(streak)
                .diaCiclo(diaCiclo)
                .ciclo(ciclo)
                .tipoProximaRecompensa(reward.getTipoReward())
                .moedasPrevistas(ehMoedas ? calcularMoedas(reward.getQuantidadeMoedasBase(), ciclo) : null)
                .tierPacotePrevisto(ehMoedas ? null : calcularTierPacote(reward.getTierPacoteBase(), ciclo))
                .build();
    }

    /** Resgata a recompensa do dia. Lança IllegalStateException se já resgatou hoje. */
    @Transactional
    public ResgateResponseDto resgatar(UUID idUser) {
        LocalDate hoje = LocalDate.now();

        if (loginRepository.existsByIdUserAndDataLogin(idUser, hoje)) {
            throw new IllegalStateException("Recompensa de hoje já foi resgatada");
        }

        UserStreak streak = atualizarStreak(idUser);
        Reward reward = buscarRecompensaDoDia(streak.getDiaCiclo());

        Integer moedas = null;
        String tier = null;
        List<Carta> cartas = null;

        if ("MOEDAS".equals(reward.getTipoReward())) {
            moedas = calcularMoedas(reward.getQuantidadeMoedasBase(), streak.getCiclo());
        } else {
            tier = calcularTierPacote(reward.getTierPacoteBase(), streak.getCiclo());
            cartas = boosterService.abrirBooster(tier);
        }

        Login login = Login.builder()
                .idLogin(UUID.randomUUID())
                .idUser(idUser)
                .diaCiclo(streak.getDiaCiclo())
                .dataLogin(hoje)
                .horarioLogin(LocalTime.now())
                .streak(streak.getStreak())
                .ciclo(streak.getCiclo())
                .moedasRecebidas(moedas)
                .tierPacoteRecebido(tier)
                .build();
        loginRepository.save(login);

        List<CartaDto> cartasDto = null;
        if (cartas != null) {
            cartasDto = cartas.stream().map(c -> {
                cartaRecebidaRepository.save(CartaRecebida.builder()
                        .id(UUID.randomUUID())
                        .idLogin(login.getIdLogin())
                        .idCarta(c.getIdCarta())
                        .raridadeSorteada(c.getRaridade())
                        .build());
                return new CartaDto(c.getIdCarta(), c.getNome(), c.getRaridade());
            }).toList();
        }

        if (moedas != null) {
            eventPublisher.publicarRecompensaDinheiro(
                    new RecompensaDinheiroEvent(idUser, moedas));
        }
        if (cartas != null) {
            var cartasGanhas = cartas.stream()
                    .collect(Collectors.groupingBy(Carta::getIdCarta, Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> new RecompensaCartaEvent.CartaGanha(e.getKey(), e.getValue().intValue()))
                    .toList();
            eventPublisher.publicarRecompensaCarta(
                    new RecompensaCartaEvent(idUser, cartasGanhas));
        }

        return ResgateResponseDto.builder()
                .tipoReward(reward.getTipoReward())
                .moedasRecebidas(moedas)
                .tierPacote(tier)
                .cartas(cartasDto)
                .streak(streak.getStreak())
                .diaCiclo(streak.getDiaCiclo())
                .ciclo(streak.getCiclo())
                .build();
    }
}