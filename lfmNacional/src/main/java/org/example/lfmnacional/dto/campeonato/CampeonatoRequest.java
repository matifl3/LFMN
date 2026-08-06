package org.example.lfmnacional.dto.campeonato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoCampeonato;

public record CampeonatoRequest(
        @NotBlank String nombre,
        String temporada,
        @NotNull Long categoriaId,
        EstadoCampeonato estado,
        String sistemaPuntos
) {
}
