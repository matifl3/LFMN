package org.example.lfmnacional.dto.categoria;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank String nombre,
        String descripcion,
        Integer eloMinimo,
        Integer eloMaximo,
        Boolean setupAbierto,
        Boolean setupFijo
) {
}
