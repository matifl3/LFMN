package org.example.lfmnacional.dto.anuncio;

import jakarta.validation.constraints.NotBlank;

public record AnuncioRequest(
        @NotBlank String titulo,
        @NotBlank String contenido,
        String urlImagen
) {
}
