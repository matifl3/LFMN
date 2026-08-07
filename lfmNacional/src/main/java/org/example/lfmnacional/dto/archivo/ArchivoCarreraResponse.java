package org.example.lfmnacional.dto.archivo;

import org.example.lfmnacional.enums.TipoArchivo;

public record ArchivoCarreraResponse(
        Long id,
        String nombre,
        String ruta,
        TipoArchivo tipo
) {
}
