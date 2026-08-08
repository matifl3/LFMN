package org.example.lfmnacional.dto.usuario;

public record LoginResponse(
        String token,
        UsuarioResponse usuario
) {
}
