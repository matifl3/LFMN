package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Vincula el Steam verificado por OpenID ({@code tokenRegistro}) a una cuenta existente. */
public record SteamVinculacionLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String tokenRegistro
) {
}
