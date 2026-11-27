package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.TipoSancion;

/**
 * Lo que una sancion le hace al mundo segun su tipo (GRASP Polimorfismo /
 * Variaciones protegidas). Antes {@code SancionService} tenia dos {@code switch}
 * paralelos sobre {@link TipoSancion}, uno para aplicar y otro para revertir;
 * ahora cada tipo es un bean y un tipo nuevo es una clase nueva, sin tocar el
 * servicio. Se registran en {@link EfectosSancion}.
 *
 * <p>Se llaman dentro de la transaccion de {@code SancionService}: navegan
 * relaciones lazy de la sancion (usuario, carrera).
 */
public interface EfectoSancion {

    TipoSancion tipo();

    void aplicar(Sancion sancion);

    /** Deshace {@link #aplicar}. Solo se llama si la sancion tiene efectos aplicados. */
    void revertir(Sancion sancion);

    /**
     * true si el efecto reordena la clasificacion de la carrera: entonces puede
     * completar logros de otros pilotos (los que suben), no solo del sancionado.
     */
    default boolean reclasificaCarrera() {
        return false;
    }
}
