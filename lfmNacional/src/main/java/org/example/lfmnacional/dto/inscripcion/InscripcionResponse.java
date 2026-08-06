package org.example.lfmnacional.dto.inscripcion;

import org.example.lfmnacional.enums.EstadoInscripcion;

import java.time.LocalDateTime;

public record InscripcionResponse(
        Long id,
        Long carreraId,
        Long usuarioId,
        EstadoInscripcion estado,
        LocalDateTime fechaInscripcion
) {
}
