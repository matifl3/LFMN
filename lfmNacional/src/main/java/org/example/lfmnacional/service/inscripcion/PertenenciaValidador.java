package org.example.lfmnacional.service.inscripcion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class PertenenciaValidador extends ValidadorInscripcion {

    private final CampeonatoAccesoService accesoService;

    @Override
    protected void validarPropio(Carrera carrera, Usuario usuario) {
        if (accesoService.puedeParticiparEnCarrera(usuario, carrera)) {
            return;
        }
        throw new BusinessException("La carrera \"" + carrera.getNombre() + "\" es de un campeonato privado. "
                + "Pedi al administrador del campeonato que te sume para poder inscribirte");
    }
}