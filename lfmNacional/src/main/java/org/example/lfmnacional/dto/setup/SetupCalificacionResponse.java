package org.example.lfmnacional.dto.setup;

public record SetupCalificacionResponse(
        Long id,
        Long setupId,
        Long usuarioId,
        Integer puntaje
) {
}
