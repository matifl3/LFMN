package org.example.lfmnacional.service.puntos;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SistemaPuntosF1 implements SistemaPuntos {

    private static final List<Integer> PUNTOS = List.of(25, 18, 15, 12, 10, 8, 6, 4, 2, 1);

    @Override
    public String clave() {
        return "F1 STANDARD";
    }

    @Override
    public String nombre() {
        return "F1";
    }

    @Override
    public int puntosPara(int posicion) {
        if (posicion < 1 || posicion > PUNTOS.size()) {
            return 0;
        }
        return PUNTOS.get(posicion - 1);
    }
}