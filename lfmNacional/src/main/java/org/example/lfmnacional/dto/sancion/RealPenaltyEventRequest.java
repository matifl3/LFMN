package org.example.lfmnacional.dto.sancion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RealPenaltyEventRequest(
        @NotBlank String eventoId,
        LocalDateTime timestamp,
        String carreraId,
        @NotBlank String driverGUID,
        @NotBlank String tipo,
        Integer segundos,
        Integer vuelta,
        String motivo,
        String sesion
) {
}
