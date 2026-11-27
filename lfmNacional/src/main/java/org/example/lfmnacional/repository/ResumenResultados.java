package org.example.lfmnacional.repository;

/**
 * Conteos de resultados de un piloto resueltos en UNA query (antes eran cinco
 * COUNT separados). Lo consumen el perfil ({@code /usuarios/{id}/stats}) y la
 * evaluacion de logros, que miden lo mismo.
 */
public record ResumenResultados(
        Long carreras,
        Long finalizadas,
        Long victorias,
        Long podios,
        Long poles
) {
}
