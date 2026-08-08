package org.example.lfmnacional.dto.logro;

import org.example.lfmnacional.enums.TipoRecompensa;

public record RecompensaResponse(
        Long id,
        Long logroId,
        String descripcion,
        TipoRecompensa tipo
) {
}
