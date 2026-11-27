package org.example.lfmnacional.dto.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Sin campo {@code archivo}: el archivo solo se asigna via POST /api/setups/{id}/archivo,
 * que genera el nombre en el servidor. Aceptarlo del cliente permitia apuntar (y al
 * borrar el setup, eliminar) el archivo de otro usuario.
 */
public record SetupRequest(
        @NotBlank String titulo,
        String descripcion,
        @NotBlank String circuito,
        @NotBlank String vehiculo,
        Long categoriaId
) {
}
