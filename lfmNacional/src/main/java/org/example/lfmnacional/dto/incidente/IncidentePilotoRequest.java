package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.RolPilotoIncidente;

public record IncidentePilotoRequest(
        @NotNull Long usuarioId,
        @NotNull RolPilotoIncidente rol
) {
}
