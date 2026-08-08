package org.example.lfmnacional.dto.logro;

import org.example.lfmnacional.enums.TipoCondicionLogro;

import java.util.List;

public record LogroResponse(
        Long id,
        String nombre,
        String descripcion,
        TipoCondicionLogro tipoCondicion,
        Integer valorCondicion,
        String icono,
        List<RecompensaResponse> recompensas
) {
}
