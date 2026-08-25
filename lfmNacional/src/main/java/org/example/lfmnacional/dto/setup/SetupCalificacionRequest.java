package org.example.lfmnacional.dto.setup;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SetupCalificacionRequest(
        @NotNull @Min(1) @Max(5) Integer puntaje
) {
}
