package org.example.lfmnacional.dto.resultado;

public record ResultadoCarreraResponse(
        Long id,
        Long carreraId,
        String carreraNombre,
        String categoriaNombre,
        Long usuarioId,
        String nombrePiloto,
        Integer posicionFinal,
        Long tiempoTotal,
        Long vueltaRapida,
        String modeloAuto,
        String skinAuto,
        Boolean poles,
        Boolean finalizo,
        Integer eloGanado,
        Integer srGanado
) {
}
