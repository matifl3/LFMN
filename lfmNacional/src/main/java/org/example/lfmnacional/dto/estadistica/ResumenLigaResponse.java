package org.example.lfmnacional.dto.estadistica;

/**
 * Numeros publicos de la liga para el hero del home. A diferencia de
 * {@link EstadisticasResponse} (solo ADMIN), no expone nada operativo.
 *
 * @param pilotosActivos    usuarios con al menos un resultado cargado (excluye cuentas sin correr, como las del seeder)
 * @param carrerasCorridas  carreras en estado FINALIZADA
 * @param categoriasActivas categorias con al menos un campeonato ACTIVO
 */
public record ResumenLigaResponse(
        long pilotosActivos,
        long carrerasCorridas,
        long categoriasActivas
) {
}
