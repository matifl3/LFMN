package org.example.lfmnacional.dto.estadistica;

public record EstadisticasResponse(
        long totalUsuarios,
        long usuariosActivos,
        long usuariosAdmin,
        long usuariosComisario,
        long totalCarreras,
        long carrerasProgramadas,
        long carrerasInscripcionesAbiertas,
        long carrerasInscripcionesCerradas,
        long carrerasEnCurso,
        long carrerasFinalizadas,
        long carrerasCanceladas,
        long totalIncidentes,
        long incidentesPendientes,
        long incidentesEnAnalisis,
        long incidentesResueltos,
        long totalSanciones,
        long totalInscripciones,
        long apelacionesPendientes,
        long setupsPublicados,
        long anuncios,
        long campeonatos,
        long categorias
) {
}