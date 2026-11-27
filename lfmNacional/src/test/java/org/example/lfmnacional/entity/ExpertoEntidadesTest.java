package org.example.lfmnacional.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Reglas que viven en las entidades (GRASP Experto en informacion). */
class ExpertoEntidadesTest {

    @Test
    void elRatingNoBajaDeCeroYDevuelveLoAplicado() {
        Usuario u = Usuario.builder().elo(1500).safetyRating(30).build();

        assertThat(u.ajustarElo(-2000)).isEqualTo(-1500);
        assertThat(u.getElo()).isZero();
        assertThat(u.ajustarSafetyRating(-10)).isEqualTo(-10);
        assertThat(u.getSafetyRating()).isEqualTo(20);
        assertThat(u.ajustarElo(25)).isEqualTo(25);
    }

    @Test
    void seRevierteLoAplicadoYEnSancionesViejasElValorPedido() {
        assertThat(Sancion.builder().valor(-200).cambioAplicado(-150).build().cambioARevertir()).isEqualTo(-150);
        assertThat(Sancion.builder().valor(-200).build().cambioARevertir()).isEqualTo(-200);
        assertThat(Sancion.builder().build().cambioARevertir()).isZero();
    }

    @Test
    void establecerPasswordInvalidaLasSesiones() {
        Usuario u = Usuario.builder().tokenVersion(2).build();

        u.establecerPassword("hash");

        assertThat(u.getPassword()).isEqualTo("hash");
        assertThat(u.isPasswordEstablecida()).isTrue();
        assertThat(u.getVersionTokens()).isEqualTo(3);
    }
}
