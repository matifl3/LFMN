package org.example.lfmnacional.service.puntos;

/**
 * Una estrategia de puntaje expuesta al frontend.
 *
 * @param clave      valor a persistir en {@code campeonato.sistemaPuntos}
 * @param nombre     texto a mostrar en el select
 * @param porDefecto si esta es la estrategia aplicada cuando el campeonato no
 *                   define una clave valida
 */
public record SistemaPuntosCatalogo(String clave, String nombre, boolean porDefecto) {
}