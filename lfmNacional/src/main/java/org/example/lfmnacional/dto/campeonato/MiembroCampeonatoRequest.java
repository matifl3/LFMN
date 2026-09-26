package org.example.lfmnacional.dto.campeonato;

import jakarta.validation.constraints.Size;

/**
 * El administrador del campeonato identifica al piloto a sumar por email o por
 * nombrePiloto. Al menos uno de los dos tiene que venir informado.
 */
public record MiembroCampeonatoRequest(
        @Size(max = 150) String email,
        @Size(max = 100) String nombrePiloto
) {
}
