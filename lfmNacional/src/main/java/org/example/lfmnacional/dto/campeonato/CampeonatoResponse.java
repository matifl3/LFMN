package org.example.lfmnacional.dto.campeonato;

import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.VisibilidadCampeonato;

public record CampeonatoResponse(
        Long id,
        String nombre,
        String temporada,
        Long categoriaId,
        String categoriaNombre,
        EstadoCampeonato estado,
        String sistemaPuntos,
        VisibilidadCampeonato visibilidad,
        Long adminId,
        String adminNombrePiloto,
        long cantidadMiembros,
        /** El usuario logueado es el administrador dueno de este campeonato. */
        boolean soyAdmin,
        /** El usuario logueado es piloto miembro de este campeonato. */
        boolean soyMiembro
) {
}
