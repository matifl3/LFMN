package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.DecisionComisario;

public record VotoRequest(
        @NotNull Long comisarioId,
        @NotNull DecisionComisario decision,
        String comentario
) {
}
