package org.example.lfmnacional.service.inscripcion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.BusinessException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
public class InscripcionesAbiertasValidador extends ValidadorInscripcion {

    public static final int MINUTOS_CIERRE_PREVIO = 5;

    @Override
    protected void validarPropio(Carrera carrera, Usuario usuario) {
        if (carrera.getEstado() != EstadoCarrera.PROGRAMADA
                && carrera.getEstado() != EstadoCarrera.INSCRIPCIONES_ABIERTAS) {
            throw new BusinessException("La carrera no tiene inscripciones abiertas");
        }
        if (!carrera.getFecha().isAfter(LocalDateTime.now().plusMinutes(MINUTOS_CIERRE_PREVIO))) {
            throw new BusinessException("Las inscripciones ya estan cerradas para esta carrera");
        }
    }
}