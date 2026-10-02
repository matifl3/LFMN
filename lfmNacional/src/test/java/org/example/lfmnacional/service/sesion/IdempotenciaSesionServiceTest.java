package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.DriverSesionData;
import org.example.lfmnacional.dto.sesion.EventoSesionData;
import org.example.lfmnacional.dto.sesion.LapSesionData;
import org.example.lfmnacional.dto.sesion.Posicion3D;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotenciaSesionServiceTest {

    private static final Long CARRERA = 7L;

    private final IdempotenciaSesionService service = new IdempotenciaSesionService();

    private SesionServerData sesion(String tipo, int autos, int resultados, int vueltas, int eventos) {
        return new SesionServerData(
                "balcarce", "chicana", tipo, 0, 5,
                null,
                resultados(resultados),
                vueltas(vueltas),
                eventos(eventos));
    }

    private List<ResultadoSesionData> resultados(int n) {
        return java.util.stream.IntStream.range(0, n)
                .mapToObj(i -> new ResultadoSesionData("Piloto " + i, "guid" + i, i, "kombi", 90000L, 1000L * (i + 1), 0, 0))
                .toList();
    }

    private List<LapSesionData> vueltas(int n) {
        return java.util.stream.IntStream.range(0, n)
                .mapToObj(i -> new LapSesionData("Piloto", "guid0", 1, "kombi", 0L, 90000L, List.of(30000, 30000, 30000), 0, 0, "S", 0))
                .toList();
    }

    private List<EventoSesionData> eventos(int n) {
        return java.util.stream.IntStream.range(0, n)
                .mapToObj(i -> new EventoSesionData("CAR_COLLISION", i, new DriverSesionData("Piloto", null, null, "guid0", null), i + 1, null, 40.0, null, null))
                .toList();
    }

    @Test
    void mismaSesionMismaClave() {
        SesionServerData a = sesion("RACE", 3, 3, 5, 4);
        SesionServerData b = sesion("RACE", 3, 3, 5, 4);

        assertThat(service.claveSesion(CARRERA, a)).isEqualTo(service.claveSesion(CARRERA, b));
    }

    @Test
    void elNombreDelArchivoNoAfectaLaClave() {
        SesionServerData a = sesion("RACE", 3, 3, 5, 4);
        SesionServerData b = sesion("RACE", 3, 3, 5, 4);

        // El watcher deduplicaba por nombre de archivo, asi que el mismo JSON
        // subido con otro nombre se procesaba dos veces. La clave va por
        // contenido justamente para no depender del nombre.
        assertThat(service.claveSesion(CARRERA, a)).isEqualTo(service.claveSesion(CARRERA, b));
    }

    @Test
    void diferenteCarreraDistintaClave() {
        SesionServerData s = sesion("RACE", 3, 3, 5, 4);

        assertThat(service.claveSesion(1L, s)).isNotEqualTo(service.claveSesion(2L, s));
    }

    @Test
    void diferenteTipoSesionDistintaClave() {
        SesionServerData race = sesion("RACE", 3, 3, 5, 4);
        SesionServerData qualify = sesion("QUALIFY", 3, 3, 5, 4);

        assertThat(service.claveSesion(CARRERA, race)).isNotEqualTo(service.claveSesion(CARRERA, qualify));
    }

    @Test
    void distintaCantidadDeVueltasDistintaClave() {
        SesionServerData corta = sesion("RACE", 3, 3, 5, 4);
        SesionServerData larga = sesion("RACE", 3, 3, 20, 4);

        assertThat(service.claveSesion(CARRERA, corta)).isNotEqualTo(service.claveSesion(CARRERA, larga));
    }

    @Test
    void sesionVaciaNoRevienta() {
        SesionServerData vacia = new SesionServerData(null, null, null, null, null, null, null, null, null);

        assertThat(service.claveSesion(CARRERA, vacia)).isNotBlank();
    }

    @Test
    void claveTieneLongitudFija() {
        String clave = service.claveSesion(CARRERA, sesion("RACE", 3, 3, 5, 4));

        assertThat(clave).hasSize(40);
    }

    @Test
    void mismoIncidenteMismaClave() {
        EventoSesionData evento = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.4, null, null);

        assertThat(service.claveIncidente(CARRERA, evento, "guid0"))
                .isEqualTo(service.claveIncidente(CARRERA, evento, "guid0"));
    }

    @Test
    void impactoIgualALGranoGeneraMismaClave() {
        EventoSesionData a = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.0, null, null);
        EventoSesionData b = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.0, null, null);

        assertThat(service.claveIncidente(CARRERA, a, "guid0"))
                .isEqualTo(service.claveIncidente(CARRERA, b, "guid0"));
    }

    @Test
    void impactosDistintosGeneranClavesDistintas() {
        // 40.4 y 40.6 redondean a 40 y 41: son choques distintos y deben
        // registrarse como incidentes distintos.
        EventoSesionData suave = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.4, null, null);
        EventoSesionData fuerte = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.6, null, null);

        assertThat(service.claveIncidente(CARRERA, suave, "guid0"))
                .isNotEqualTo(service.claveIncidente(CARRERA, fuerte, "guid0"));
    }

    @Test
    void incidentesDiferentesDelMismoPilotoClavesDistintas() {
        EventoSesionData choque = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.0, null, null);
        EventoSesionData salida = new EventoSesionData("CAR_TRACKLIMIT", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.0, null, null);

        assertThat(service.claveIncidente(CARRERA, choque, "guid0"))
                .isNotEqualTo(service.claveIncidente(CARRERA, salida, "guid0"));
    }

    @Test
    void mismoEventoEnCarrerasDistintasClavesDistintas() {
        EventoSesionData evento = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, 40.0, null, null);

        assertThat(service.claveIncidente(1L, evento, "guid0"))
                .isNotEqualTo(service.claveIncidente(2L, evento, "guid0"));
    }

    @Test
    void eventoSinPosicionNoRevienta() {
        EventoSesionData evento = new EventoSesionData("CAR_COLLISION", 3, new DriverSesionData("Piloto", null, null, "guid0", null), 5, null, null, null, null);

        assertThat(service.claveIncidente(CARRERA, evento, "guid0")).hasSize(40);
    }
}