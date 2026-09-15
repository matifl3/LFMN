package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SteamVinculacionLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String guidSteam
) {
}