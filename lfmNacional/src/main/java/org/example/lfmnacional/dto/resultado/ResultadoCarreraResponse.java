package org.example.lfmnacional.dto.resultado;

public record ResultadoCarreraResponse(
        Long id,
        Long carreraId,
        Long usuarioId,
        Integer posicionFinal,
        Long tiempoTotal,
        Long vueltaRapida,
        Boolean poles,
        Boolean finalizo,
        Integer eloGanado,
        Integer srGanado
) {
}
