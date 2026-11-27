package org.example.lfmnacional.service.sancion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.TipoSancion;
import org.springframework.stereotype.Component;

/** Perdida de puestos: la reclasificacion lee las sanciones activas y baja al piloto. */
@Component
@RequiredArgsConstructor
public class EfectoPuestos implements EfectoSancion {

    private final ReclasificacionCarrera reclasificacion;

    @Override
    public TipoSancion tipo() {
        return TipoSancion.PUESTOS;
    }

    @Override
    public void aplicar(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        reclasificacion.resultadoDe(sancion);   // sin resultado en la carrera, no aplica
        reclasificacion.reclasificar(sancion.getCarrera());
    }

    @Override
    public void revertir(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        // reclasificar() aplica las sanciones activas: la que se revierte ya no cuenta.
        sancion.setEfectosAplicados(false);
        reclasificacion.reclasificar(sancion.getCarrera());
    }

    @Override
    public boolean reclasificaCarrera() {
        return true;
    }
}
