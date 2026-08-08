package org.example.lfmnacional.dto.carrera;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoCarrera;

import java.time.LocalDateTime;

public record CarreraRequest(
        @NotBlank String nombre,
        @NotNull LocalDateTime fecha,
        @NotBlank String circuito,
        @NotNull Long categoriaId,
        EstadoCarrera estado,
        Integer cupoMaximo,
        String servidor,
        String contrasenaServidor,
        Long archivoId
) {
}
