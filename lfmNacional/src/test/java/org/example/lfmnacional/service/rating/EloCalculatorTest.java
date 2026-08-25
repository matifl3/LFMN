package org.example.lfmnacional.service.rating;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EloCalculatorTest {

    private final EloCalculator calculator = new EloCalculator();

    @Test
    void mismoEloYGanaMitadDelCampo() {
        int cambio = calculator.calcularCambio(1500, 1, 2, List.of(1500));
        assertThat(cambio).isEqualTo(16);
    }

    @Test
    void mismoEloYPierdeMitadDelCampo() {
        int cambio = calculator.calcularCambio(1500, 2, 2, List.of(1500));
        assertThat(cambio).isEqualTo(-16);
    }

    @Test
    void favoritoContraCampoMasDebil() {
        int cambio = calculator.calcularCambio(1500, 1, 4, List.of(1300, 1300, 1300));
        assertThat(cambio).isGreaterThan(0);
    }

    @Test
    void debilContraCampoMasFuerte() {
        int cambio = calculator.calcularCambio(1500, 4, 4, List.of(1700, 1700, 1700));
        assertThat(cambio).isLessThan(0);
    }

    @Test
    void unSoloParticipanteDaCero() {
        int cambio = calculator.calcularCambio(1500, 1, 1, List.of(1500));
        assertThat(cambio).isEqualTo(0);
    }

    @Test
    void campoVacioUsaEloPropio() {
        int cambio = calculator.calcularCambio(1500, 1, 2, List.of());
        assertThat(cambio).isEqualTo(16);
    }

    @Test
    void tercerLugarEnCarreraGrande() {
        int cambio = calculator.calcularCambio(1500, 3, 20, List.of(1500, 1400, 1600, 1300, 1450, 1550, 1350, 1650, 1200, 1700));
        assertThat(cambio).isGreaterThan(0);
    }

    @Test
    void ultimoLugarEnCarreraGrande() {
        int cambio = calculator.calcularCambio(1500, 20, 20, List.of(1500, 1400, 1600, 1300, 1450, 1550, 1350, 1650, 1200, 1700));
        assertThat(cambio).isLessThan(0);
    }

    @Test
    void eloNuloLanzaNPE() {
        assertThatThrownBy(() -> calculator.calcularCambio(null, 1, 2, List.of(1500)))
                .isInstanceOf(NullPointerException.class);
    }
}
