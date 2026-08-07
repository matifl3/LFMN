package org.example.lfmnacional.service.rating;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EloCalculator {

    private static final int K = 32;
    private static final int DISPERSION = 400;

    public int calcularCambio(Integer eloPropio, int posicion, int totalParticipantes, List<Integer> elosRivales) {
        double promedioRivales = elosRivales.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(eloPropio.doubleValue());
        double esperado = 1.0 / (1.0 + Math.pow(10, (promedioRivales - eloPropio) / (double) DISPERSION));
        double resultado = totalParticipantes <= 1 ? 0.5
                : (double) (totalParticipantes - posicion) / (double) (totalParticipantes - 1);
        return (int) Math.round(K * (resultado - esperado));
    }
}
