package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.DecisionComisario;

import java.time.LocalDateTime;

public record DecisionComisarioResponse(
        Long incidenteId,
        DecisionComisario decision,
        String comentario,
        LocalDateTime fecha,
        String tipo
) {
}
