package com.quatro.rewards_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.quatro.rewards_service.service.RewardsService;

class RewardsServiceTest {

    private final RewardsService service = new RewardsService(null, null, null, null, null, null);

    @Test
    void moedasSemCicloNaoMultiplica() {
        assertEquals(50, service.calcularMoedas(50, 0));
    }

    @Test
    void moedasComUmCicloMultiplicaPor13() {
        assertEquals(650, service.calcularMoedas(50, 1));
    }

    @Test
    void moedasRespeitamTetoDe5000() {
        assertEquals(5000, service.calcularMoedas(600, 1));
        assertEquals(5000, service.calcularMoedas(50, 3));
    }

    @Test
    void tierSobeUmPorCiclo() {
        assertEquals("NORMAL", service.calcularTierPacote("BASICO", 1));
        assertEquals("EPICO",  service.calcularTierPacote("NORMAL", 2));
    }

    @Test
    void tierNaoPassaDeLendario() {
        assertEquals("LENDARIO", service.calcularTierPacote("EPICO", 10));
        assertEquals("LENDARIO", service.calcularTierPacote("LENDARIO", 1));
    }
}