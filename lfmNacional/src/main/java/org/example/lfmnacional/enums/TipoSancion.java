package org.example.lfmnacional.enums;

/**
 * Tipos de sancion que el backend sabe aplicar.
 *
 * <p>DRIVE_THROUGH y STOP_AND_GO se eliminaron: nunca Predijeron efecto (ni
 * points, ni Elo, ni SR) y soloexistieron como opciones en el select de la UI.
 * Quitar los del enum tambien rompe la lectura de las filas que ya tuvieran
 * esos valores, asi que hay que limpiarlos de la base antes de deployar:
 * {@code DELETE FROM sancion WHERE tipo IN ('DRIVE_THROUGH','STOP_AND_GO')}.
 */
public enum TipoSancion {
    PUESTOS,
    SEGUNDOS,
    DESCALIFICACION,
    ELO,
    SAFETY_RATING
}
