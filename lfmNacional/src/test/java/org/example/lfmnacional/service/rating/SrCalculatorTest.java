package org.example.lfmnacional.service.rating;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SrCalculatorTest {

    private final SrCalculator calculator = new SrCalculator();

    @Test
    void dnfRetornaPenalizacion() {
        assertThat(calculator.calcularCambio(false, 1)).isEqualTo(-5);
    }

    @Test
    void victoriaMasBonus() {
        assertThat(calculator.calcularCambio(true, 1)).isEqualTo(15);
    }

    @Test
    void segundoMasPodio() {
        assertThat(calculator.calcularCambio(true, 2)).isEqualTo(13);
    }

    @Test
    void terceroMasBonusTercero() {
        assertThat(calculator.calcularCambio(true, 3)).isEqualTo(11);
    }

    @Test
    void cuartoSoloFinalizo() {
        assertThat(calculator.calcularCambio(true, 4)).isEqualTo(5);
    }

    @Test
    void decimoSoloFinalizo() {
        assertThat(calculator.calcularCambio(true, 10)).isEqualTo(5);
    }

    @Test
    void dnfSinImportarPosicion() {
        assertThat(calculator.calcularCambio(false, 5)).isEqualTo(-5);
        assertThat(calculator.calcularCambio(false, 10)).isEqualTo(-5);
    }

    @Test
    void victoriaDnfNoAplicaBonusVictoria() {
        assertThat(calculator.calcularCambio(false, 1)).isEqualTo(-5);
    }
}
