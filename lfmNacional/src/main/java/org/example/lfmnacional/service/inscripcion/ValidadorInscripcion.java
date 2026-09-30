package org.example.lfmnacional.service.inscripcion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;

public abstract class ValidadorInscripcion {

    private ValidadorInscripcion siguiente;

    public void setSiguiente(ValidadorInscripcion siguiente) {
        this.siguiente = siguiente;
    }

    public final void validar(Carrera carrera, Usuario usuario) {
        validarPropio(carrera, usuario);
        if (siguiente != null) {
            siguiente.validar(carrera, usuario);
        }
    }

    protected abstract void validarPropio(Carrera carrera, Usuario usuario);
}