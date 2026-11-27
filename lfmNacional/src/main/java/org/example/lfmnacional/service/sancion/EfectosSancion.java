package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.enums.TipoSancion;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registro de {@link EfectoSancion} por tipo (mismo esquema que
 * {@code SistemaPuntosFactory}). Spring inyecta todos los beans que implementan
 * la interfaz; dos para el mismo tipo es un error de configuracion y corta el
 * arranque.
 *
 * <p>Un tipo sin efecto registrado (hoy DESCALIFICACION) se guarda y notifica
 * pero no mueve rating ni clasificacion, igual que antes del refactor.
 */
@Component
public class EfectosSancion {

    private final Map<TipoSancion, EfectoSancion> porTipo = new EnumMap<>(TipoSancion.class);

    public EfectosSancion(List<EfectoSancion> efectos) {
        for (EfectoSancion efecto : efectos) {
            EfectoSancion previo = porTipo.put(efecto.tipo(), efecto);
            if (previo != null) {
                throw new IllegalStateException("Dos efectos para la sancion " + efecto.tipo() + ": "
                        + previo.getClass().getSimpleName() + " y " + efecto.getClass().getSimpleName());
            }
        }
    }

    public Optional<EfectoSancion> de(TipoSancion tipo) {
        return Optional.ofNullable(tipo == null ? null : porTipo.get(tipo));
    }
}
