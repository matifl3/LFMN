package org.example.lfmnacional.enums;

public enum VisibilidadCampeonato {
    /**
     * Visible por todos. La inscripcion a sus carreras solo exige caer en el rango
     * Elo de la categoria.
     */
    PUBLICO,
    /**
     * Solo pueden inscribirse los pilotos que el administrador del campeonato agrego
     * como miembros. Sin filtro de Elo.
     */
    PRIVADO
}
