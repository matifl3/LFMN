package org.example.lfmnacional.dto.logro;

import org.example.lfmnacional.enums.TipoRecompensa;

import java.time.LocalDateTime;

public record UsuarioRecompensaResponse(
        Long recompensaId,
        Long logroId,
        String descripcion,
        TipoRecompensa tipo,
        Boolean reclamada,
        LocalDateTime fecha
) {
}
