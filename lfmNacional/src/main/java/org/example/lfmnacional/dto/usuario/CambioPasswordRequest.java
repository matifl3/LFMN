package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Size;
import org.example.lfmnacional.entity.Usuario;
import jakarta.validation.constraints.NotBlank;

public record CambioPasswordRequest(
        String passwordActual,
        @NotBlank @Size(min = Usuario.MIN_PASSWORD, max = 100) String nuevaPassword
) {
}
