package org.example.lfmnacional.dto.vuelta;

public record VueltaResponse(
        Long id,
        Long carreraId,
        Long usuarioId,
        String nombrePiloto,
        Integer numeroVuelta,
        Long tiempoMs,
        Long sector1,
        Long sector2,
        Long sector3,
        Integer cortes,
        String neumatico,
        String tipo
) {
}
