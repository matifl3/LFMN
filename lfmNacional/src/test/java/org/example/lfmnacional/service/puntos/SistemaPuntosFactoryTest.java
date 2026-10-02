package org.example.lfmnacional.service.puntos;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SistemaPuntosFactoryTest {

    private final SistemaPuntosFactory factory = new SistemaPuntosFactory(
            List.of(new SistemaPuntosF1(), new SistemaPuntosTop10()));

    @Test
    void resuelvePorClaveExacta() {
        assertThat(factory.de("TOP 10")).isInstanceOf(SistemaPuntosTop10.class);
        assertThat(factory.de("F1 STANDARD")).isInstanceOf(SistemaPuntosF1.class);
    }

    @Test
    void normalizaCaseYEspacios() {
        assertThat(factory.de("top 10")).isInstanceOf(SistemaPuntosTop10.class);
        assertThat(factory.de("  Top 10  ")).isInstanceOf(SistemaPuntosTop10.class);
        assertThat(factory.de("f1 standard")).isInstanceOf(SistemaPuntosF1.class);
    }

    @Test
    void claveDesconocidaOCaNullCaenAlDefault() {
        // Estas son las claves que el frontend envie antes de este fix.
        assertThat(factory.de("F1_SPRINT")).isInstanceOf(SistemaPuntosF1.class);
        assertThat(factory.de("F1")).isInstanceOf(SistemaPuntosF1.class);
        assertThat(factory.de(null)).isInstanceOf(SistemaPuntosF1.class);
        assertThat(factory.de("   ")).isInstanceOf(SistemaPuntosF1.class);
        assertThat(factory.de("")).isInstanceOf(SistemaPuntosF1.class);
        //(systema_puntos es un VARCHAR(100) libre: el admin puede escribir cualquier
        // texto y tiene que caerse al default, no romper el calculo de puntos.)
        assertThat(factory.de("25 puntos al ganador")).isInstanceOf(SistemaPuntosF1.class);
    }

    @Test
    void defaultEsF1YEstaMarcadoEnElCatalogo() {
        assertThat(factory.clavePorDefecto()).isEqualTo("F1 STANDARD");
        assertThat(factory.disponibles())
                .filteredOn(SistemaPuntosCatalogo::porDefecto)
                .singleElement()
                .extracting(SistemaPuntosCatalogo::clave)
                .isEqualTo("F1 STANDARD");
    }

    @Test
    void catalogoExponeTodasLasEstrategiasConSusClavesCanonicas() {
        // Son las claves que el frontend persiste; no deben cambiar sin
        // una migracion de campeonato.sistema_puntos.
        assertThat(factory.disponibles())
                .extracting(SistemaPuntosCatalogo::clave)
                .containsExactly("F1 STANDARD", "TOP 10");
        assertThat(factory.disponibles())
                .extracting(SistemaPuntosCatalogo::nombre)
                .containsExactly("F1", "Top 10");
    }

    @Test
    void fallaArranqueSiNoEstaLaEstrategiaDefault() {
        // Sin esta, un borrado accidental de SistemaPuntosF1 dejaria la app
        // arrancando y solo explotaria al calcular puntos de un campeonato.
        SistemaPuntosFactory sinDefault = new SistemaPuntosFactory(List.of(new SistemaPuntosTop10()));
        assertThatThrownBy(sinDefault::clavePorDefecto)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("F1 STANDARD");
    }

    @Test
    void top10Otorga10AlPrimeroYCeroFueraDelRango() {
        SistemaPuntos top10 = factory.de("TOP 10");
        assertThat(top10.puntosPara(1)).isEqualTo(10);
        assertThat(top10.puntosPara(10)).isEqualTo(1);
        assertThat(top10.puntosPara(11)).isZero();
        assertThat(top10.puntosPara(0)).isZero();
    }

    @Test
    void f1PuntuaSoloLasPrimerasDiezPosiciones() {
        // F1 es el default: su tabla de puntos es el valor mas sensible del
        // calculo de campeonato, asi que se congela aqui.
        SistemaPuntos f1 = factory.de("F1 Standard");
        assertThat(f1.puntosPara(1)).isEqualTo(25);
        assertThat(f1.puntosPara(2)).isEqualTo(18);
        assertThat(f1.puntosPara(3)).isEqualTo(15);
        assertThat(f1.puntosPara(10)).isEqualTo(1);
        assertThat(f1.puntosPara(11)).isZero();
        assertThat(f1.puntosPara(0)).isZero();
    }
}