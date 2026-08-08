package org.example.lfmnacional.dto.setup;

import java.time.LocalDateTime;

public record SetupResponse(
        Long id,
        String titulo,
        String descripcion,
        String circuito,
        String vehiculo,
        String archivo,
        Long autorId,
        Long categoriaId,
        LocalDateTime fechaPublicacion,
        Double promedioCalificacion
) {
}
