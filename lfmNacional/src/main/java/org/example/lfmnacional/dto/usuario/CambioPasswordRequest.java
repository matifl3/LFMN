package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record CambioPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank String nuevaPassword
) {
}
