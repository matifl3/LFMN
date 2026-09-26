package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unico lugar donde se decide que puede ver y que puede tocar cada usuario sobre
 * un campeonato. No se puede resolver con {@code @PreAuthorize} porque "es dueno
 * de este campeonato" depende del recurso, no del token.
 */
@Service
@RequiredArgsConstructor
public class CampeonatoAccesoService {

    private final CampeonatoMiembroRepository miembroRepository;

    // ---------------------------------------------------------------- roles

    public boolean esAdminGlobal(Usuario usuario) {
        return usuario != null && usuario.getRol() == Rol.ADMIN;
    }

    public boolean esComisario(Usuario usuario) {
        return usuario != null && usuario.getRol() == Rol.COMISARIO;
    }

    public boolean esAdminCampeonato(Usuario usuario) {
        return usuario != null && usuario.getRol() == Rol.ADMIN_CAMPEONATO;
    }

    /** Puede crear campeonato y, en el caso del ADMIN global, cualquiera. */
    public boolean puedeCrearCampeonato(Usuario usuario) {
        return esAdminGlobal(usuario) || esAdminCampeonato(usuario);
    }

    // -------------------------------------------------------- relaciones

    /** ADMIN global, o ADMIN_CAMPEONATO dueño de este campeonato puntual. */
    public boolean administra(Usuario usuario, Campeonato campeonato) {
        if (usuario == null || campeonato == null) {
            return false;
        }
        if (esAdminGlobal(usuario)) {
            return true;
        }
        if (esAdminCampeonato(usuario) && campeonato.getAdmin() != null) {
            return campeonato.getAdmin().getId().equals(usuario.getId());
        }
        return false;
    }

    public boolean administraCarrera(Usuario usuario, Carrera carrera) {
        return carrera != null && administra(usuario, carrera.getCampeonato());
    }

    // -------------------------------------------------------- membresia

    /** Ids de los campeonato en los que el usuario es piloto miembro. Una sola query. */
    @Transactional(readOnly = true)
    public Set<Long> campeonatoIdsDondeEsMiembro(Long usuarioId) {
        if (usuarioId == null) {
            return Set.of();
        }
        return miembroRepository.findByUsuario_Id(usuarioId).stream()
                .map(m -> m.getCampeonato().getId())
                .collect(Collectors.toSet());
    }

    public boolean esMiembro(Usuario usuario, Campeonato campeonato) {
        if (usuario == null || campeonato == null) {
            return false;
        }
        return miembroRepository.existsByCampeonato_IdAndUsuario_Id(campeonato.getId(), usuario.getId());
    }

    // ------------------------------------------------------ visibilidade

    /**
     * El campeonato en si (nombre, temporada, categoria, calendario) es publico
     * siempre. Lo privado es el contenido: roster de pilotos, tabla de puntos,
     * detalle de carrera, resultados y telemetria. Eso es lo que exige membresia
     * cuando la visibilidad es PRIVADO.
     */
    public boolean veContenido(Usuario usuario, Campeonato campeonato) {
        if (campeonato == null) {
            return false;
        }
        if (campeonato.getVisibilidad() == VisibilidadCampeonato.PUBLICO) {
            return true;
        }
        if (administra(usuario, campeonato) || esComisario(usuario)) {
            return true;
        }
        return esMiembro(usuario, campeonato);
    }

    public boolean veCarrera(Usuario usuario, Carrera carrera) {
        return carrera != null && veContenido(usuario, carrera.getCampeonato());
    }

    /**
     * Poder COMPETIR es mas estrecho que poder VER. El comisario tiene lectura de
     * los privados por su labor, pero no compite: si se dejara pasar por
     * {@link #veCarrera} terminaria inscripto en un campeonato que no organiza.
     */
    public boolean puedeParticipar(Usuario usuario, Campeonato campeonato) {
        if (campeonato == null || usuario == null) {
            return false;
        }
        return administra(usuario, campeonato) || esMiembro(usuario, campeonato);
    }

    public boolean puedeParticiparEnCarrera(Usuario usuario, Carrera carrera) {
        return carrera != null && puedeParticipar(usuario, carrera.getCampeonato());
    }

    public void exigirVeContenido(Usuario usuario, Campeonato campeonato) {
        if (!veContenido(usuario, campeonato)) {
            throw new BusinessException("El campeonato '" + campeonato.getNombre()
                    + "' es privado. Solo los pilotos que agrego el administrador pueden ver su contenido");
        }
    }

    public void exigirVeCarrera(Usuario usuario, Carrera carrera) {
        if (!veCarrera(usuario, carrera)) {
            throw new BusinessException("La carrera '" + carrera.getNombre()
                    + "' pertenece a un campeonato privado del que no sos miembro");
        }
    }

    // ---------------------------------------------------------- escritura

    public void exigirAdministra(Usuario usuario, Campeonato campeonato) {
        if (campeonato == null) {
            throw new ResourceNotFoundException("Campeonato no encontrado");
        }
        if (!administra(usuario, campeonato)) {
            throw new BusinessException("No administras el campeonato '" + campeonato.getNombre() + "'");
        }
    }

    public void exigirAdministraCarrera(Usuario usuario, Carrera carrera) {
        if (carrera == null) {
            throw new ResourceNotFoundException("Carrera no encontrada");
        }
        exigirAdministra(usuario, carrera.getCampeonato());
    }
}
