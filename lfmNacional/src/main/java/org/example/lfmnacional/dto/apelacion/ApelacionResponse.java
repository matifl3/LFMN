package org.example.lfmnacional.dto.apelacion;

import org.example.lfmnacional.enums.EstadoApelacion;

import java.time.LocalDateTime;

public record ApelacionResponse(
        Long id,
        Long sancionId,
        Long usuarioId,
        String nombrePiloto,
        String motivo,
        EstadoApelacion estado,
        String respuestaAdmin,
        LocalDateTime fecha
) {
}
