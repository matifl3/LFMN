package org.example.lfmnacional.dto.notificacion;

import org.example.lfmnacional.enums.TipoNotificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        Long usuarioId,
        TipoNotificacion tipo,
        String mensaje,
        Boolean leida,
        LocalDateTime fecha,
        String link
) {
}
