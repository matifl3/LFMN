package org.example.lfmnacional.dto.usuario;

import org.example.lfmnacional.enums.Rol;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String email,
        String nombrePiloto,
        String fotoPerfil,
        String guidSteam,
        Integer elo,
        Integer safetyRating,
        Rol rol,
        LocalDateTime fechaRegistro
) {
}
