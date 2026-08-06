package org.example.lfmnacional.dto.inscripcion;

import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.EstadoInscripcion;

public record InscripcionRequest(
        @NotNull Long carreraId,
        @NotNull Long usuarioId,
        EstadoInscripcion estado
) {
}
