package org.example.lfmnacional.dto.campeonato;

import java.time.LocalDateTime;

public record MiembroCampeonatoResponse(
        Long usuarioId,
        String nombrePiloto,
        String fotoPerfil,
        Integer elo,
        Integer safetyRating,
        LocalDateTime fechaAlta
) {
}