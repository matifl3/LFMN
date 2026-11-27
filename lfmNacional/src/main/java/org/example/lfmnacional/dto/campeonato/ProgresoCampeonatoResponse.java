package org.example.lfmnacional.dto.campeonato;

/**
 * Avance de la temporada de un campeonato.
 *
 * @param rondasTotales carreras del campeonato sin contar las CANCELADA
 * @param rondasCorridas carreras del campeonato en estado FINALIZADA
 */
public record ProgresoCampeonatoResponse(
        long rondasTotales,
        long rondasCorridas
) {
}
