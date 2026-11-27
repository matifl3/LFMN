package org.example.lfmnacional.dto.usuario;

import org.example.lfmnacional.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 100) String nombrePiloto,
        @NotBlank @Size(min = Usuario.MIN_PASSWORD, max = 100) String password
) {
}