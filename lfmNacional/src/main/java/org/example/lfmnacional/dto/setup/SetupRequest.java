package org.example.lfmnacional.dto.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetupRequest(
        @NotBlank String titulo,
        String descripcion,
        @NotBlank String circuito,
        @NotBlank String vehiculo,
        String archivo,
        @NotNull Long autorId,
        Long categoriaId
) {
}
