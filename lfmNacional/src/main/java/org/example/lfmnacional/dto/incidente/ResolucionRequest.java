package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.dto.sancion.SancionRequest;

import java.util.List;

public record ResolucionRequest(
        @NotNull Long comisarioId,
        @NotBlank String explicacion,
        List<SancionRequest> sanciones
) {
}
