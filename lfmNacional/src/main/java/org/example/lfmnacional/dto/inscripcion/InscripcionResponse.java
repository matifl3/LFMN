package org.example.lfmnacional.dto.inscripcion;

import org.example.lfmnacional.enums.EstadoInscripcion;

import java.time.LocalDateTime;

public record InscripcionResponse(
        Long id,
        Long carreraId,
        String carreraNombre,
        String categoriaNombre,
        LocalDateTime carreraFecha,
        Long usuarioId,
        String nombrePiloto,
        String fotoPerfil,
        Integer elo,
        Integer safetyRating,
        EstadoInscripcion estado,
        LocalDateTime fechaInscripcion
) {
}
