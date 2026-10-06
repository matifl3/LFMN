package org.example.lfmnacional.dto.campeonato;

import java.util.List;

/**
 * Resumen estadistico de un campeonato, para quien lo administra.
 *
 * <p>Los puntos y las posiciones NO se recalculan aca: salen de
 * {@code CampeonatoPosicion}, que es la tabla que ya usa el endpoint de la tabla.
 *
 * <p>Sin cache a proposito, igual que EstadisticasResponse: son agregados que
 * cambian en cada import de sesion y no vale la pena un cache que hay que
 * acordarse de evictar.
 */
public record EstadisticasCampeonatoResponse(
        long campeonatoId,
        String nombre,
        String temporada,
        String categoriaNombre,
        String estado,
        long carrerasTotales,
        long carrerasFinalizadas,
        long carrerasEnCurso,
        long carrerasProgramadas,
        long carrerasCanceladas,
        long pilotosHabilitados,
        long pilotosConParticipacion,
        long participaciones,
        long abandono,
        double porcentajeFinalizacion,
        long incidentes,
        long incidentesPendientes,
        long sanciones,
        List<RondaCampeonatoEstadisticasResponse> rondas,
        List<PilotoCampeonatoEstadisticasResponse> pilotos
) {
}