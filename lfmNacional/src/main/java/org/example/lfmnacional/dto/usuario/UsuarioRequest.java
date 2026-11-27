package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * {@code password} es la password ACTUAL: se exige solo cuando el propio usuario
 * cambia su email (con un token robado no se puede desviar la recuperacion).
 */
public record UsuarioRequest(
        @NotBlank @Email String email,
        String password,
        String nombrePiloto
) {
}
