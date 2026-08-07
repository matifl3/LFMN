package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record SteamRequest(
        @NotBlank String guidSteam
) {
}
