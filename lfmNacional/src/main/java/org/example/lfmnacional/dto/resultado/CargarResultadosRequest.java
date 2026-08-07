package org.example.lfmnacional.dto.resultado;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CargarResultadosRequest(
        @NotNull Long carreraId,
        @NotEmpty List<@Valid ResultadoCarreraRequest> resultados
) {
}
