package org.example.lfmnacional.dto.campeonato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.lfmnacional.entity.Campeonato;
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
        VisibilidadCampeonato visibilidad,
        /**
         * En el PUT, {@code null} (o ausente) conserva la descripcion actual: asi un
         * cliente que no conoce el campo (o el "Cerrar/Reabrir" del admin, que arma
         * el body a mano) no la borra. Para quitarla se manda {@code ""}.
         */
        @Size(max = Campeonato.MAX_DESCRIPCION) String descripcion
) {
}
