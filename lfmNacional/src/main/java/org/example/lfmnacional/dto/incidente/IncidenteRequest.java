package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoIncidente;

public record IncidenteRequest(
        @NotNull Long carreraId,
        Integer vuelta,
        String descripcion,
        String videoUrl
) {
}
