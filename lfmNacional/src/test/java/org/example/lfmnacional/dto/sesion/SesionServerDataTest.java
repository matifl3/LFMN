package org.example.lfmnacional.dto.sesion;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SesionServerDataTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseaSesionQualify() throws Exception {
        SesionServerData sesion = leer("/sesiones/2026_8_7_16_15_QUALIFY.json");

        assertEquals("balcarce", sesion.trackName());
        assertEquals("chicana", sesion.trackConfig());
        assertEquals("QUALIFY", sesion.type());
        assertEquals(3, sesion.cars().size());
        assertEquals(3, sesion.result().size());
        assertEquals(2, sesion.laps().size());
        assertEquals(2, sesion.events().size());

        CarSesionData auto = sesion.cars().get(0);
        assertNotNull(auto.driver());
        assertEquals("Matifel", auto.driver().name());
        assertEquals("76561199066767489", auto.driver().guid());

        ResultadoSesionData resultado = sesion.result().get(0);
        assertEquals("Matifel", resultado.driverName());
        assertEquals("76561199066767489", resultado.driverGuid());
        assertEquals(999999999L, resultado.bestLap());

        EventoSesionData evento = sesion.events().get(0);
        assertEquals("COLLISION_WITH_ENV", evento.type());
        assertEquals(0, evento.carId());
        assertEquals(84.53349, evento.impactSpeed(), 0.001);
        assertNotNull(evento.worldPosition());
        assertEquals(-40.13552, evento.worldPosition().x(), 0.001);
    }

    @Test
    void parseaSesionRace() throws Exception {
        SesionServerData sesion = leer("/sesiones/2026_8_7_16_31_RACE.json");

        assertEquals("RACE", sesion.type());
        assertEquals(5, sesion.raceLaps());
        assertEquals(3, sesion.result().size());
        assertEquals(5, sesion.laps().size());

        ResultadoSesionData resultado = sesion.result().get(0);
        assertEquals(110800L, resultado.bestLap());
        assertEquals(827739L, resultado.totalTime());

        LapSesionData vuelta = sesion.laps().get(0);
        assertEquals(3, vuelta.sectors().size());
        assertEquals(0, vuelta.cuts());
        assertEquals("S", vuelta.tyre());
    }

    private SesionServerData leer(String ruta) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(ruta)) {
            assertNotNull(in, "Recurso no encontrado: " + ruta);
            return objectMapper.readValue(in, SesionServerData.class);
        }
    }
}
