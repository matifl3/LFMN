package org.example.lfmnacional.service.rating;

import org.springframework.stereotype.Component;

@Component
public class SrCalculator {

    private static final int BONUS_FINALIZO = 5;
    private static final int PENALIDAD_DNF = -5;
    private static final int BONUS_VICTORIA = 10;
    private static final int BONUS_PODIO = 8;
    private static final int BONUS_TERCERO = 6;

    public int calcularCambio(boolean finalizo, int posicion) {
        int cambio = finalizo ? BONUS_FINALIZO : PENALIDAD_DNF;
        if (finalizo && posicion == 1) {
            cambio += BONUS_VICTORIA;
        } else if (finalizo && posicion == 2) {
            cambio += BONUS_PODIO;
        } else if (finalizo && posicion == 3) {
            cambio += BONUS_TERCERO;
        }
        return cambio;
    }
}
