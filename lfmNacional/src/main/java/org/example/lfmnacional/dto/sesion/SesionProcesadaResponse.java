package org.example.lfmnacional.dto.sesion;

import java.time.LocalDateTime;

public record SesionProcesadaResponse(
        Long id,
        Long carreraId,
        String nombreArchivo,
        String tipo,
        LocalDateTime fechaProcesamiento
) {
}
