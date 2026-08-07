package org.example.lfmnacional.dto.usuario;

import java.time.LocalDateTime;

public record SafetyRatingHistorialResponse(
        Long id,
        Integer cambio,
        String motivo,
        LocalDateTime fecha,
        Long carreraId
) {
}
