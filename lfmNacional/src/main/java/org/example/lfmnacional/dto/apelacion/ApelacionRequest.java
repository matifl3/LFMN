package org.example.lfmnacional.dto.apelacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApelacionRequest(
        @NotNull Long sancionId,
        @NotBlank String motivo
) {
}
