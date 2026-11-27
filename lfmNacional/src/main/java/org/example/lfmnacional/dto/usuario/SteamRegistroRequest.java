package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de cuenta desde el ingreso con Steam. {@code tokenRegistro} es el JWT que
 * emite el callback OpenID (scope steam-registro): el GUID sale de ahi, nunca del cliente.
 */
public record SteamRegistroRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 100) String nombrePiloto,
        @NotBlank String tokenRegistro
) {
}
