package org.example.lfmnacional.dto.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetupComentarioRequest(
        @NotNull Long usuarioId,
        @NotBlank String texto
) {
}
