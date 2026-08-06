package org.example.lfmnacional.dto.categoria;

public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion,
        Integer eloMinimo,
        Integer eloMaximo,
        Boolean setupAbierto,
        Boolean setupFijo
) {
}
