package org.example.lfmnacional.dto.vuelta;

public record VueltaAnalisisResponse(
        Long id,
        Integer numeroVuelta,
        Long tiempoMs,
        Long sector1,
        Long sector2,
        Long sector3,
        Integer cortes,
        String neumatico,
        Long deltaLiderMs,
        Integer posicionEnVuelta
) {
}