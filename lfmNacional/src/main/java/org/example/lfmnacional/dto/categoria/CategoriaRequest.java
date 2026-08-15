package org.example.lfmnacional.dto.categoria;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank String nombre,
        String descripcion,
        @Min(0) Integer eloMinimo,
        @Min(0) Integer eloMaximo,
        Boolean setupAbierto,
        Boolean setupFijo
) {
}
