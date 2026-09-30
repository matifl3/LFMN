package org.example.lfmnacional.service.inscripcion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InscripcionValidacion {

    private final List<ValidadorInscripcion> validadores;

    public void validar(Carrera carrera, Usuario usuario) {
        ValidadorInscripcion cabeza = encadenar();
        if (cabeza != null) {
            cabeza.validar(carrera, usuario);
        }
    }

    private ValidadorInscripcion encadenar() {
        ValidadorInscripcion cabeza = null;
        ValidadorInscripcion anterior = null;
        for (ValidadorInscripcion validador : validadores) {
            if (cabeza == null) {
                cabeza = validador;
            }
            if (anterior != null) {
                anterior.setSiguiente(validador);
            }
            anterior = validador;
        }
        return cabeza;
    }
}