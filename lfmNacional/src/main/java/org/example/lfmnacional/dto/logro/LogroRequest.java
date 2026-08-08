package org.example.lfmnacional.dto.logro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.lfmnacional.enums.TipoCondicionLogro;

public record LogroRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull TipoCondicionLogro tipoCondicion,
        @NotNull @Positive Integer valorCondicion,
        String icono
) {
}
