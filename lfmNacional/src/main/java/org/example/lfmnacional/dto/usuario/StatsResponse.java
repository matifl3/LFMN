package org.example.lfmnacional.dto.usuario;

public record StatsResponse(
        long carrerasDisputadas,
        long victorias,
        long podios,
        long poles,
        long vueltasRapidas,
        double porcentajeFinalizacion
) {
}
