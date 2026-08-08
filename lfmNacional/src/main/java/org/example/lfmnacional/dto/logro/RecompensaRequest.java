package org.example.lfmnacional.dto.logro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.TipoRecompensa;

public record RecompensaRequest(
        @NotBlank String descripcion,
        @NotNull TipoRecompensa tipo
) {
}
