package org.example.lfmnacional.dto.campeonato;

import java.time.LocalDateTime;

/**
 * Una fila de la tabla "por ronda" de las estadisticas del campeonato.
 *
 * <p>{@code presentes} son los pilotos con resultado cargado; {@code ausentes} son
 * los que se anotaron y no aparecen. Ese contraste es el dato que le sirve al
 * organizador para saber si el calendario se esta cumpliendo.
 */
public record RondaCampeonatoEstadisticasResponse(
        long carreraId,
        String nombre,
        String circuito,
        LocalDateTime fecha,
        String estado,
        int cupoMaximo,
        long inscriptos,
        long presentes,
        long ausentes,
        long abandono,
        long incidentes,
        long sanciones,
        String ganador,
        Long mejorVueltaMs,
        String mejorVueltaPiloto
) {
}