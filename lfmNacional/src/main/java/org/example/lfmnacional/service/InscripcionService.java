package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.inscripcion.InscripcionResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.EntityMapper;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private static final int MINUTOS_CIERRE_PREVIO = 5;

    private final InscripcionRepository inscripcionRepository;
    private final CarreraService carreraService;
    private final UsuarioService usuarioService;
    private final CampeonatoAccesoService accesoService;

    public Inscripcion getEntity(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada con id " + id));
    }

    public Carrera carrera(Long carreraId) {
        return carreraService.getEntity(carreraId);
    }

    @Transactional
    public InscripcionResponse inscribirse(Long carreraId, Long usuarioId) {
        Carrera carrera = carreraService.getEntity(carreraId);
        Usuario usuario = usuarioService.getEntity(usuarioId);
        validarInscripcionesAbiertas(carrera);
        validarPertenencia(carrera, usuario);
        validarRequisitosElo(carrera, usuario);

        Optional<Inscripcion> existente = inscripcionRepository.findByCarrera_IdAndUsuario_Id(carreraId, usuarioId);
        if (existente.isPresent() && existente.get().getEstado() != EstadoInscripcion.CANCELADA) {
            throw new BusinessException("El usuario ya esta inscripto en esta carrera");
        }

        EstadoInscripcion estado = hayCupo(carrera) ? EstadoInscripcion.INSCRIPTO : EstadoInscripcion.LISTA_ESPERA;

        if (existente.isPresent()) {
            Inscripcion previa = existente.get();
            previa.setEstado(estado);
            return toResponse(inscripcionRepository.save(previa));
        }

        Inscripcion inscripcion = Inscripcion.builder()
                .carrera(carrera)
                .usuario(usuario)
                .estado(estado)
                .build();
        return toResponse(inscripcionRepository.save(inscripcion));
    }

    @Transactional
    public InscripcionResponse baja(Long id) {
        Inscripcion inscripcion = getEntity(id);
        if (inscripcion.getEstado() == EstadoInscripcion.CANCELADA) {
            throw new BusinessException("La inscripcion ya fue cancelada");
        }
        boolean fueInscripto = inscripcion.getEstado() == EstadoInscripcion.INSCRIPTO;
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(inscripcion);
        if (fueInscripto) {
            promoverListaDeEspera(inscripcion.getCarrera().getId());
        }
        return toResponse(inscripcion);
    }

    @Transactional
    public InscripcionResponse cancelar(Long carreraId, Long usuarioId) {
        Carrera carrera = carreraService.getEntity(carreraId);
        if (!carrera.getFecha().isAfter(LocalDateTime.now().plusMinutes(MINUTOS_CIERRE_PREVIO))) {
            throw new BusinessException("Las inscripciones ya estan cerradas para esta carrera");
        }
        Inscripcion inscripcion = inscripcionRepository.findByCarrera_IdAndUsuario_Id(carreraId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe inscripcion del usuario en la carrera"));
        return baja(inscripcion.getId());
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarPorCarrera(Long carreraId, Usuario usuario) {
        Carrera carrera = carreraService.getEntity(carreraId);
        accesoService.exigirVeCarrera(usuario, carrera);
        return inscripcionRepository.findByCarrera_Id(carreraId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarPorUsuario(Long usuarioId) {
        return inscripcionRepository.findByUsuario_Id(usuarioId).stream().map(this::toResponse).toList();
    }

    /**
     * El calendario es publico, asi que el count no puede exigir membresia. A los
     * que no son miembros de un campeonato privado se les reporta 0 para no
     * delatar cuantos pilotos hay adentro.
     */
    public long countInscriptos(Long carreraId, Usuario usuario) {
        Carrera carrera = carreraService.getEntity(carreraId);
        if (!accesoService.veCarrera(usuario, carrera)) {
            return 0L;
        }
        return inscripcionRepository.countByCarrera_IdAndEstado(carreraId, EstadoInscripcion.INSCRIPTO);
    }

    private boolean hayCupo(Carrera carrera) {
        if (carrera.getCupoMaximo() == null) {
            return true;
        }
        long inscriptos = inscripcionRepository.countByCarrera_IdAndEstado(carrera.getId(), EstadoInscripcion.INSCRIPTO);
        return inscriptos < carrera.getCupoMaximo();
    }

    private void validarInscripcionesAbiertas(Carrera carrera) {
        if (carrera.getEstado() != EstadoCarrera.PROGRAMADA
                && carrera.getEstado() != EstadoCarrera.INSCRIPCIONES_ABIERTAS) {
            throw new BusinessException("La carrera no tiene inscripciones abiertas");
        }
        if (!carrera.getFecha().isAfter(LocalDateTime.now().plusMinutes(MINUTOS_CIERRE_PREVIO))) {
            throw new BusinessException("Las inscripciones ya estan cerradas para esta carrera");
        }
    }

    /**
     * Un campeonato privado es de lista cerrada: solo se puede inscribir quien el
     * administrador del campeonato sumo como miembro. El propio administrador
     * compite siempre; un comisario puede ver pero no competir.
     */
    private void validarPertenencia(Carrera carrera, Usuario usuario) {
        if (accesoService.puedeParticiparEnCarrera(usuario, carrera)) {
            return;
        }
        throw new BusinessException("La carrera \"" + carrera.getNombre() + "\" es de un campeonato privado. "
                + "Pedi al administrador del campeonato que te sume para poder inscribirte");
    }

    /** Sin filtro de Elo en los privados: manda la lista que armo el admin. */
    private void validarRequisitosElo(Carrera carrera, Usuario usuario) {
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

    private void promoverListaDeEspera(Long carreraId) {
        Inscripcion siguiente = inscripcionRepository
                .findByCarrera_IdAndEstado(carreraId, EstadoInscripcion.LISTA_ESPERA).stream()
                .min(Comparator.comparing(Inscripcion::getFechaInscripcion))
                .orElse(null);
        if (siguiente != null) {
            siguiente.setEstado(EstadoInscripcion.INSCRIPTO);
            inscripcionRepository.save(siguiente);
        }
    }

    private InscripcionResponse toResponse(Inscripcion inscripcion) {
        var c = EntityMapper.resolveCarreraInfo(inscripcion.getCarrera());
        var u = EntityMapper.resolveUsuarioBasico(inscripcion.getUsuario());
        return new InscripcionResponse(
                inscripcion.getId(),
                c.id(),
                c.nombre(),
                c.categoriaNombre(),
                inscripcion.getCarrera().getFecha(),
                u.id(),
                u.nombrePiloto(),
                u.fotoPerfil(),
                inscripcion.getUsuario().getElo(),
                inscripcion.getUsuario().getSafetyRating(),
                inscripcion.getEstado(),
                inscripcion.getFechaInscripcion());
    }
}
