package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record RestablecerPasswordRequest(
        @NotBlank String token,
        @NotBlank String nuevaPassword
) {
}