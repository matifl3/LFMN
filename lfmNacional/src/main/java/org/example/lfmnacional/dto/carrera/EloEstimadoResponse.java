package org.example.lfmnacional.dto.carrera;

import java.util.List;

public record EloEstimadoResponse(
        int posicionEsperada,
        int deltaEsperado,
        int deltaMejorCaso,
        int deltaPeorCaso,
        List<PosicionElo> detalle
) {
    public record PosicionElo(int posicion, int deltaElo) {
    }
}