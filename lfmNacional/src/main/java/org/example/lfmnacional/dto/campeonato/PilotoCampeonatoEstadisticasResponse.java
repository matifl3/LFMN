package org.example.lfmnacional.dto.campeonato;

/**
 * Una fila de la tabla "por piloto" de las estadisticas del campeonato.
 *
 * <p>{@code posicion} y {@code puntos} vienen de la tabla de posiciones ya
 * calculada ({@code CampeonatoPosicion}); el resto se agrega desde
 * {@code ResultadoCarrera}. Un piloto que no disputo ninguna carrera aparece con
 * la fila en cero desde {@code CampeonatoMiembro}.
 *
 * @param eloGanado   delta de Elo acumulado en este campeonato
 * @param srGanado    delta de Safety Rating acumulado en este campeonato
 * @param incidentes  incidentes con rol CAUSANTE (los que le pasaron no cuentan)
 */
public record PilotoCampeonatoEstadisticasResponse(
        long usuarioId,
        String nombrePiloto,
        String fotoPerfil,
        int elo,
        int safetyRating,
        int posicion,
        int puntos,
        long carrerasDisputadas,
        long victorias,
        long podios,
        long poles,
        long vueltasRapidas,
        long abandono,
        double porcentajeFinalizacion,
        int eloGanado,
        int srGanado,
        long incidentes,
        long sanciones
) {
}