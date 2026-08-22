package org.example.lfmnacional.dto.anuncio;

import java.time.LocalDateTime;

public record AnuncioResponse(
        Long id,
        String titulo,
        String contenido,
        String urlImagen,
        LocalDateTime fecha,
        Boolean destacado
) {
}
