package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.DecisionComisario;

import java.time.LocalDateTime;

public record ResolucionResponse(
        Long id,
        Long incidenteId,
        Long comisarioId,
        String explicacion,
        LocalDateTime fecha
) {
}
