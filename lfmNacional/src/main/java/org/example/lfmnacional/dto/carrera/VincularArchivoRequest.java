package org.example.lfmnacional.dto.carrera;

import jakarta.validation.constraints.NotNull;

public record VincularArchivoRequest(
        @NotNull Long archivoId
) {
}
