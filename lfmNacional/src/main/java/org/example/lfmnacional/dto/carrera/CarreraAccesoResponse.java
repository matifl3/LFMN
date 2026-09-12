package org.example.lfmnacional.dto.carrera;

public record CarreraAccesoResponse(
        Long carreraId,
        String servidor,
        String contrasenaServidor
) {
}