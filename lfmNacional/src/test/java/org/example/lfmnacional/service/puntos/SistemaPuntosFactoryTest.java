package org.example.lfmnacional.service.puntos;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SistemaPuntosFactoryTest {

    private final SistemaPuntosFactory factory =
            new SistemaPuntosFactory(List.of(new SistemaPuntosF1(), new SistemaPuntosTop10()));

    @Test
    void resuelvePorClaveNormalizada() {
        assertThat(factory.de("f1 STANDARD")).isInstanceOf(SistemaPuntosF1.class);
        assertThat(factory.de("TOP 10")).isInstanceOf(SistemaPuntosTop10.class);
    }

    @Test
    void claveDesconocidaVuelveAlSistemaPorDefecto() {
        assertThat(factory.de("25 puntos al ganador")).isInstanceOf(SistemaPuntosF1.class);
    }

    @Test
    void claveNulaVuelveAlSistemaPorDefecto() {
        assertThat(factory.de(null)).isInstanceOf(SistemaPuntosF1.class);
    }

    @Test
    void f1PuntuaSoloLasPrimerasDiezPosiciones() {
        SistemaPuntos sistema = factory.de("F1 Standard");
        assertThat(sistema.puntosPara(1)).isEqualTo(25);
        assertThat(sistema.puntosPara(10)).isEqualTo(1);
        assertThat(sistema.puntosPara(11)).isZero();
        assertThat(sistema.puntosPara(0)).isZero();
    }
}