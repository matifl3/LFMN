package org.example.lfmnacional.enums;

public enum Rol {
    USUARIO,
    ADMIN,
    COMISARIO,
    /**
     * Creador de campeonato privado. Puede tener varios campeonato y es el unico
     * que suma pilotos a los suyos. No administra usuarios, categorias ni anuncios.
     */
    ADMIN_CAMPEONATO
}
