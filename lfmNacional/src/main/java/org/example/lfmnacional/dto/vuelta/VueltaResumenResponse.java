package org.example.lfmnacional.dto.vuelta;

public record VueltaResumenResponse(
        Long mejorVueltaMs,
        Integer numeroVueltaMejor,
        Long mejorS1,
        Long mejorS2,
        Long mejorS3,
        Long teoricaMs,
        Long potencialMs,
        Long mejorS1Parrilla,
        Long mejorS2Parrilla,
        Long mejorS3Parrilla,
        Long teoricaParrillaMs,
        Integer vueltasTotales,
        Long mediaMs,
        Long desvioMs,
        Integer dentroDe500ms,
        Integer dentroDe1s,
        Integer posicionGrilla,
        Integer posicionFinal,
        Integer posicionesGanadas,
        Integer posicionPico
) {
}