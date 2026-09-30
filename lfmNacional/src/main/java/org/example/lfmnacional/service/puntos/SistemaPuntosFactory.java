package org.example.lfmnacional.service.puntos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SistemaPuntosFactory {

    private final List<SistemaPuntos> estrategias;

    public SistemaPuntos de(String clave) {
        String normalizada = normalizar(clave);
        for (SistemaPuntos estrategia : estrategias) {
            if (normalizar(estrategia.clave()).equals(normalizada)) {
                return estrategia;
            }
        }
        return porDefecto();
    }

    private SistemaPuntos porDefecto() {
        return estrategias.stream()
                .filter(e -> e instanceof SistemaPuntosF1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay estrategia de puntos por defecto"));
    }

    private String normalizar(String clave) {
        if (clave == null) {
            return "";
        }
        return clave.trim().toLowerCase(Locale.ROOT);
    }
}