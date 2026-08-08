package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotBlank;

public record ResolucionUpdateRequest(
        @NotBlank String explicacion
) {
}
