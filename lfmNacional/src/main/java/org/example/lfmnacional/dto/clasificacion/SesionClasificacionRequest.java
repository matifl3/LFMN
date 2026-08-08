package org.example.lfmnacional.dto.clasificacion;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SesionClasificacionRequest(
        @NotNull Long carreraId,
        @NotNull Long usuarioId,
        @NotNull Long tiempo,
        Long diferenciaPole,
        LocalDateTime fecha
) {
}
