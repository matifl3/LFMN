package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportadorSesionTest {

    private static final Carrera CARRERA = Carrera.builder().build();
    private static final SesionServerData SESION = new SesionServerData(null, null, null, null, null, null, null, null, null);

    static class Grabador extends ImportadorSesion {

        boolean validado;
        boolean procesado;

        Grabador() {
            super("PRUEBA");
        }

        @Override
        protected void validar(SesionServerData sesion, Carrera carrera) {
            validado = true;
        }

        @Override
        protected void procesar(Carrera carrera, SesionServerData sesion) {
            procesado = true;
        }
    }

    @Test
    void elEsqueletoValidaYLuegoProcesa() {
        Grabador grabador = new Grabador();

        String tipo = grabador.importar(CARRERA, SESION);

        assertThat(tipo).isEqualTo("PRUEBA");
        assertThat(grabador.validado).describedAs("el hook validar corre primero").isTrue();
        assertThat(grabador.procesado).describedAs("el paso procesar corre despues").isTrue();
    }

    @Test
    void unaSubclaseSinHookDeValidacionProcesaIgual() {
        ImportadorSesion sinValidacion = new ImportadorSesion("X") {
            @Override
            protected void procesar(Carrera carrera, SesionServerData sesion) {
            }
        };

        assertThat(sinValidacion.tipo()).isEqualTo("X");
        assertThat(sinValidacion.importar(CARRERA, SESION)).isEqualTo("X");
    }
}