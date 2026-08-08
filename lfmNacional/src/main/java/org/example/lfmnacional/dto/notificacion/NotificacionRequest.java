package org.example.lfmnacional.dto.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.TipoNotificacion;

public record NotificacionRequest(
        @NotNull Long usuarioId,
        @NotNull TipoNotificacion tipo,
        @NotBlank String mensaje,
        String link
) {
}
