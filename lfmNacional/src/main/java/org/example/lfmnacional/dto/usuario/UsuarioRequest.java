package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String nombrePiloto,
        String fotoPerfil,
        String guidSteam
) {
}
