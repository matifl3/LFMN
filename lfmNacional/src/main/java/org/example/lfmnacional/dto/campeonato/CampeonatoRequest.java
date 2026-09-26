package org.example.lfmnacional.dto.campeonato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.VisibilidadCampeonato;

public record CampeonatoRequest(
        @NotBlank String nombre,
        String temporada,
        @NotNull Long categoriaId,
        EstadoCampeonato estado,
        String sistemaPuntos,
        /**
         * Solo lo puede setear el ADMIN global. Un ADMIN_CAMPEONATO siempre crea
         * en privado: el servicio ignora lo que venga por acá.
         */
        VisibilidadCampeonato visibilidad
) {
}
