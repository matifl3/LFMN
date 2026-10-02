package org.example.lfmnacional.service.puntos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Resuelve la estrategia de puntaje de un campeonato a partir de la clave
 * guardada en {@code campeonato.sistema_puntos}.
 *
 * <p>Las estrategias se descubren por el contenedor de Spring, asi que agregar
 * un esquema nuevo es crear un {@code @Component} que implemente
 * {@link SistemaPuntos}: no hay que tocar el factory ni el frontend, que
 * consume {@link #disponibles()}.
 */
@Component
@RequiredArgsConstructor
public class SistemaPuntosFactory {

    /** Clave de la estrategia usada cuando el campeonato no define una valida. */
    private static final String CLAVE_POR_DEFECTO = "F1 STANDARD";

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

    /**
     * Catalogo de estrategias para el frontend, ordenado por clave para que la
     * respuesta sea estable entre llamadas. Cada entrada trae {@code porDefecto}
     * para que el cliente sepa cual se aplica cuando el campeonato no tiene una
     * clave valida.
     */
    public List<SistemaPuntosCatalogo> disponibles() {
        return estrategias.stream()
                .sorted(Comparator.comparing(e -> normalizar(e.clave())))
                .map(e -> new SistemaPuntosCatalogo(
                        e.clave(),
                        e.nombre(),
                        normalizar(e.clave()).equals(normalizar(CLAVE_POR_DEFECTO))))
                .toList();
    }

    public String clavePorDefecto() {
        return porDefecto().clave();
    }

    private SistemaPuntos porDefecto() {
        return estrategias.stream()
                .filter(e -> normalizar(e.clave()).equals(normalizar(CLAVE_POR_DEFECTO)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay estrategia de puntos con clave " + CLAVE_POR_DEFECTO));
    }

    private String normalizar(String clave) {
        if (clave == null) {
            return "";
        }
        return clave.trim().toLowerCase(Locale.ROOT);
    }
}