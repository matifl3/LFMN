package org.example.lfmnacional.dto.setup;

import java.time.LocalDateTime;

public record SetupComentarioResponse(
        Long id,
        Long setupId,
        Long usuarioId,
        String texto,
        LocalDateTime fecha
) {
}
