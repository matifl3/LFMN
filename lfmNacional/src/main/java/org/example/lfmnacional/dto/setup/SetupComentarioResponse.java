package org.example.lfmnacional.dto.setup;

import java.time.LocalDateTime;

public record SetupComentarioResponse(
        Long id,
        Long setupId,
        Long usuarioId,
        String nombrePiloto,
        String fotoPerfil,
        String texto,
        LocalDateTime fecha
) {
}
