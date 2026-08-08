package org.example.lfmnacional.dto.clasificacion;

import java.time.LocalDateTime;

public record SesionClasificacionResponse(
        Long id,
        Long carreraId,
        Long usuarioId,
        LocalDateTime fecha,
        Long tiempo,
        Long diferenciaPole
) {
}
