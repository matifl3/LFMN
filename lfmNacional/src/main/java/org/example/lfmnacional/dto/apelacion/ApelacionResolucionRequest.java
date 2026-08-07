package org.example.lfmnacional.dto.apelacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoApelacion;

public record ApelacionResolucionRequest(
        @NotNull EstadoApelacion estado,
        @NotBlank String respuestaAdmin
) {
}
