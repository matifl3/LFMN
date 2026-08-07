package org.example.lfmnacional.dto.campeonato;

public record TablaPosicionResponse(
        Long usuarioId,
        String nombrePiloto,
        Integer puntos,
        Integer posicion
) {
}
