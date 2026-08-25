package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.EstadoIncidente;

import java.time.LocalDateTime;

public record IncidenteResponse(
        Long id,
        Long carreraId,
        String carreraNombre,
        String categoriaNombre,
        Long reportanteId,
        String reportanteNombre,
        Integer vuelta,
        String descripcion,
        String videoUrl,
        LocalDateTime fecha,
        EstadoIncidente estado,
        int quorumRequerido
) {
}
