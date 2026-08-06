package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.EstadoIncidente;

public record IncidenteResponse(
        Long id,
        Long carreraId,
        Long reportanteId,
        Integer vuelta,
        String descripcion,
        String videoUrl,
        EstadoIncidente estado
) {
}
