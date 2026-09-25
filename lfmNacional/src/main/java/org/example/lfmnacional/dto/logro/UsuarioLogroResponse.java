package org.example.lfmnacional.dto.logro;

import org.example.lfmnacional.enums.TipoCondicionLogro;

import java.time.LocalDateTime;

public record UsuarioLogroResponse(
        Long logroId,
        String nombre,
        String descripcion,
        TipoCondicionLogro tipoCondicion,
        Integer valorCondicion,
        String icono,
        Integer progreso,
        Boolean obtenido,
        LocalDateTime fechaObtencion
) {
}
