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
         * Solo lo puede setear el ADMIN global, y tiene que apuntar a una cuenta
         * con rol ADMIN_CAMPEONATO. Es la unica via por la que un organizador
         * llega a tener un campeonato: el ADMIN_CAMPEONATO no crea nada, administra
         * lo que le asignan. Enviar {@code null} deja el campeonato sin dueno
         * (publico de liga); enviar el id quita el dueno anterior.
         */
        Long adminId,
        VisibilidadCampeonato visibilidad
) {
}
