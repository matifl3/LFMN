package org.example.lfmnacional.dto.sancion;

import jakarta.validation.constraints.NotNull;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.TipoSancion;

import java.time.LocalDateTime;

public record SancionRequest(
        @NotNull Long usuarioId,
        Long carreraId,
        Long resolucionId,
        @NotNull TipoSancion tipo,
        Integer valor,
        String motivo,
        @NotNull OrigenSancion origen,
        String idExterno,
        LocalDateTime fecha
) {
}
