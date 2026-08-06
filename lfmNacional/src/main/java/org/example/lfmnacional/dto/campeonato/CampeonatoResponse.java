package org.example.lfmnacional.dto.campeonato;

import org.example.lfmnacional.enums.EstadoCampeonato;

public record CampeonatoResponse(
        Long id,
        String nombre,
        String temporada,
        Long categoriaId,
        String categoriaNombre,
        EstadoCampeonato estado,
        String sistemaPuntos
) {
}
