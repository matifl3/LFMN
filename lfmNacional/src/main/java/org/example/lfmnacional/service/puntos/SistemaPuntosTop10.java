package org.example.lfmnacional.service.puntos;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SistemaPuntosTop10 implements SistemaPuntos {

    private static final List<Integer> PUNTOS = List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);

    @Override
    public String clave() {
        return "TOP 10";
    }

    @Override
    public String nombre() {
        return "Top 10";
    }

    @Override
    public int puntosPara(int posicion) {
        if (posicion < 1 || posicion > PUNTOS.size()) {
            return 0;
        }
        return PUNTOS.get(posicion - 1);
    }
}