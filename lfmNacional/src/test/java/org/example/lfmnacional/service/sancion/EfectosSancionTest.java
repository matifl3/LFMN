package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.TipoSancion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EfectosSancionTest {

    /** Efecto de mentira: solo importa el tipo para el registro. */
    private record Falso(TipoSancion tipo) implements EfectoSancion {
        @Override
        public void aplicar(Sancion sancion) {
        }

        @Override
        public void revertir(Sancion sancion) {
        }
    }

    @Test
    void devuelveElEfectoDeCadaTipo() {
        Falso elo = new Falso(TipoSancion.ELO);
        EfectosSancion efectos = new EfectosSancion(List.of(elo, new Falso(TipoSancion.PUESTOS)));

        assertThat(efectos.de(TipoSancion.ELO)).containsSame(elo);
    }

    @Test
    void unTipoSinEfectoNoHaceNada() {
        EfectosSancion efectos = new EfectosSancion(List.of(new Falso(TipoSancion.ELO)));

        assertThat(efectos.de(TipoSancion.DESCALIFICACION)).isEmpty();
        assertThat(efectos.de(null)).isEmpty();
    }

    @Test
    void dosEfectosParaElMismoTipoEsUnErrorDeConfiguracion() {
        assertThatThrownBy(() -> new EfectosSancion(List.of(new Falso(TipoSancion.ELO), new Falso(TipoSancion.ELO))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ELO");
    }

    @Test
    void soloPuestosYSegundosReclasificanLaCarrera() {
        assertThat(new EfectoPuestos(null).reclasificaCarrera()).isTrue();
        assertThat(new EfectoSegundos(null, null).reclasificaCarrera()).isTrue();
        assertThat(new EfectoElo(null, null).reclasificaCarrera()).isFalse();
        assertThat(new EfectoSafetyRating(null, null).reclasificaCarrera()).isFalse();
    }
}
