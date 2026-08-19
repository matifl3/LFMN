package org.example.lfmnacional.dto.incidente;

import org.example.lfmnacional.enums.RolPilotoIncidente;

public record IncidentePilotoResponse(
        Long id,
        Long incidenteId,
        Long usuarioId,
        String nombrePiloto,
        RolPilotoIncidente rol
) {
}
