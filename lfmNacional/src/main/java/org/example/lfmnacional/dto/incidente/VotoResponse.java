package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.DecisionComisario;

import java.time.LocalDateTime;

public record VotoResponse(
        Long id,
        Long incidenteId,
        Long comisarioId,
        DecisionComisario decision,
        String comentario,
        LocalDateTime fecha
) {
}
