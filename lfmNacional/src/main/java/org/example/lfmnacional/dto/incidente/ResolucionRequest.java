package org.example.lfmnacional.dto.incidente;

import jakarta.validation.constraints.NotBlank;
import org.example.lfmnacional.dto.sancion.SancionRequest;

import java.util.List;

public record ResolucionRequest(
        @NotBlank String explicacion,
        List<SancionRequest> sanciones
) {
}
