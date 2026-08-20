package org.example.lfmnacional.dto.carrera;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoCarrera;

import java.time.LocalDateTime;

public record CarreraRequest(
        @NotBlank String nombre,
        @NotNull @FutureOrPresent LocalDateTime fecha,
        @NotBlank String circuito,
        @NotNull Long campeonatoId,
        EstadoCarrera estado,
        Integer cupoMaximo,
        String servidor,
        String contrasenaServidor,
        Long archivoId,
        String linkPista,
        String linkAuto
) {
}
