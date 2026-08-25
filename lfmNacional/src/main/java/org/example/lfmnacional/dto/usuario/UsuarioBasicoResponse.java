package org.example.lfmnacional.dto.usuario;

public record UsuarioBasicoResponse(
        Long id,
        String nombrePiloto,
        String fotoPerfil,
        Integer elo,
        Integer safetyRating
) {
}
