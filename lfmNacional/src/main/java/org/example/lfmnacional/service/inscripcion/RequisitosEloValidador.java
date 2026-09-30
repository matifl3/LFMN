package org.example.lfmnacional.service.inscripcion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class RequisitosEloValidador extends ValidadorInscripcion {

    @Override
    protected void validarPropio(Carrera carrera, Usuario usuario) {
        if (carrera.getCampeonato().getVisibilidad() == VisibilidadCampeonato.PRIVADO) {
            return;
        }
        Integer eloMinimo = carrera.getCampeonato().getCategoria().getEloMinimo();
        Integer eloMaximo = carrera.getCampeonato().getCategoria().getEloMaximo();
        if (eloMinimo != null && usuario.getElo() < eloMinimo) {
            throw new BusinessException("El Elo del usuario (" + usuario.getElo()
                    + ") es menor al minimo de la categoria (" + eloMinimo + ")");
        }
        if (eloMaximo != null && usuario.getElo() > eloMaximo) {
            throw new BusinessException("El Elo del usuario (" + usuario.getElo()
                    + ") supera el maximo de la categoria (" + eloMaximo + ")");
        }
    }
}