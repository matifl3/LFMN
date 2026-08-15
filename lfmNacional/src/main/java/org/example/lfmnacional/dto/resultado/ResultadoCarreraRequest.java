package org.example.lfmnacional.dto.resultado;

import jakarta.validation.constraints.NotNull;

public record ResultadoCarreraRequest(
        @NotNull Long carreraId,
        @NotNull Long usuarioId,
        Integer posicionFinal,
        Long tiempoTotal,
        Long vueltaRapida,
        Boolean poles,
        Boolean finalizo,
        Integer eloGanado,
        Integer srGanado,
        String modeloAuto,
        String skinAuto
) {
}
