package org.example.lfmnacional.dto.sancion;

import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.TipoSancion;

import java.time.LocalDateTime;

public record SancionResponse(
        Long id,
        Long usuarioId,
        Long carreraId,
        String carreraNombre,
        String categoriaNombre,
        Long resolucionId,
        TipoSancion tipo,
        Integer valor,
        String motivo,
        OrigenSancion origen,
        String idExterno,
        LocalDateTime fecha
) {
}
