package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;

public abstract class ImportadorSesion {

    private final String tipo;

    protected ImportadorSesion(String tipo) {
        this.tipo = tipo;
    }

    public final String tipo() {
        return tipo;
    }

    public final String importar(Carrera carrera, SesionServerData sesion) {
        validar(sesion, carrera);
        procesar(carrera, sesion);
        return tipo;
    }

    protected void validar(SesionServerData sesion, Carrera carrera) {
    }

    protected abstract void procesar(Carrera carrera, SesionServerData sesion);
}