package org.example.lfmnacional.dto.carrera;

import org.example.lfmnacional.enums.EstadoCarrera;

import java.time.LocalDateTime;

public record CarreraResponse(
        Long id,
        String nombre,
        LocalDateTime fecha,
        String circuito,
        Long campeonatoId,
        String campeonatoNombre,
        Long categoriaId,
        String categoriaNombre,
        EstadoCarrera estado,
        Integer cupoMaximo,
        String servidor,
        String contrasenaServidor,
        Long archivoId,
        String archivoNombre,
        String linkPista,
        String linkAuto
) {
}
