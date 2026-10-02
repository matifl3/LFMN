package org.example.lfmnacional.service.puntos;

public interface SistemaPuntos {

    /**
     * Clave canonica con la que se persiste en campeonato.sistema_puntos.
     * Se compara normalizada (trim + lowercase) en SistemaPuntosFactory.
     */
    String clave();

    /**
     * Nombre para mostrar en la UI. Puede diferir de la clave para que la
     * clave sea estable en BD y el texto se pueda reescribir sin migracion.
     */
    String nombre();

    int puntosPara(int posicion);
}