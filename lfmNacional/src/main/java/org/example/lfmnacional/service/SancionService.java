package org.example.lfmnacional.service;

import org.example.lfmnacional.service.sancion.EfectoSancion;
import org.example.lfmnacional.service.sancion.EfectosSancion;
import org.example.lfmnacional.entity.Usuario;
import org.springframework.data.domain.PageImpl;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.entity.*;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.EntityMapper;
import org.example.lfmnacional.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class SancionService {

    private final SancionRepository sancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final NotificacionRepository notificacionRepository;
    private final ApelacionRepository apelacionRepository;
    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final UsuarioService usuarioService;
    private final CarreraService carreraService;
    private final LogroService logroService;
    private final CampeonatoAccesoService accesoService;
    private final EfectosSancion efectos;

    public Sancion getEntity(Long id) {
        return sancionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sancion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SancionResponse getById(Long id, Usuario visor) {
        Sancion sancion = getEntity(id);
        accesoService.exigirVeCarreraSiHay(visor, sancion.getCarrera());
        return toResponse(sancion);
    }

    /** Filtra las de campeonatos privados: la pagina puede traer menos que {@code size}. */
    @Transactional(readOnly = true)
    public Page<SancionResponse> listAll(Pageable pageable, Usuario visor) {
        Page<Sancion> pagina = sancionRepository.findAll(pageable);
        return new PageImpl<>(visibles(pagina.getContent(), visor), pageable, pagina.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<SancionResponse> listarPorUsuario(Long usuarioId, Usuario visor) {
        return visibles(sancionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId), visor);
    }

    @Transactional(readOnly = true)
    public List<SancionResponse> listarPorCarrera(Long carreraId, Usuario visor) {
        accesoService.exigirVeCarrera(visor, carreraService.getEntity(carreraId));
        return sancionRepository.findByCarrera_Id(carreraId).stream()
                .map(this::toResponse).toList();
    }

    private List<SancionResponse> visibles(List<Sancion> sanciones, Usuario visor) {
        var ve = accesoService.filtroVeCarrera(visor);
        return sanciones.stream().filter(s -> ve.test(s.getCarrera())).map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public SancionResponse create(SancionRequest request) {
        Sancion sancion = buildSancion(request);
        sancion = sancionRepository.save(sancion);
        aplicarEfectos(sancion);
        notificar(sancion);
        return toResponse(sancion);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public SancionResponse update(Long id, SancionRequest request) {
        Sancion sancion = getEntity(id);
        Usuario nuevoUsuario = usuarioService.getEntity(request.usuarioId());
        Carrera nuevaCarrera = request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null;

        boolean afectaEfectos = sancion.getTipo() != request.tipo()
                || !Objects.equals(sancion.getValor(), request.valor())
                || !Objects.equals(sancion.getUsuario().getId(), nuevoUsuario.getId())
                || !Objects.equals(
                        sancion.getCarrera() != null ? sancion.getCarrera().getId() : null,
                        request.carreraId());
        if (afectaEfectos) {
            revertirEfectos(sancion);
        }

        sancion.setUsuario(nuevoUsuario);
        sancion.setCarrera(nuevaCarrera);
        sancion.setTipo(request.tipo());
        sancion.setValor(request.valor());
        sancion.setMotivo(request.motivo());
        sancion.setOrigen(request.origen());
        sancion.setIdExterno(request.idExterno());
        sancion.setFecha(request.fecha() != null ? request.fecha() : sancion.getFecha());
        sancion = sancionRepository.save(sancion);

        if (afectaEfectos) {
            aplicarEfectos(sancion);
        }
        return toResponse(sancion);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id) {
        Sancion sancion = getEntity(id);
        if (apelacionRepository.existsBySancion_Id(id)) {
            throw new BusinessException("No se puede eliminar la sancion porque tiene apelaciones asociadas");
        }
        if (sancion.getResolucion() != null) {
            throw new BusinessException(
                    "No se puede eliminar la sancion porque esta asociada a una resolucion de incidente");
        }
        revertirEfectos(sancion);
        sancionRepository.delete(sancion);
    }

    private Sancion buildSancion(SancionRequest request) {
        Usuario usuario = usuarioService.getEntity(request.usuarioId());
        Carrera carrera = request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null;
        ResolucionIncidente resolucion = request.resolucionId() != null
                ? resolucionIncidenteRepository.findById(request.resolucionId())
                        .orElse(null)
                : null;
        return Sancion.builder()
                .usuario(usuario)
                .carrera(carrera)
                .resolucion(resolucion)
                .tipo(request.tipo())
                .valor(request.valor())
                .motivo(request.motivo())
                .origen(request.origen())
                .idExterno(request.idExterno())
                .fecha(request.fecha() != null ? request.fecha() : LocalDateTime.now())
                .build();
    }

    /** El efecto concreto lo decide el tipo: ver {@link EfectoSancion} y sus implementaciones. */
    private void aplicarEfectos(Sancion sancion) {
        sancion.setEfectosAplicados(true);
        efectos.de(sancion.getTipo()).ifPresent(efecto -> efecto.aplicar(sancion));
        reevaluarLogros(sancion);
    }

    /**
     * Revertir es escribir elo/safetyRating al reves, asi que tambien tiene que
     * evictar "usuarios".
     *
     * Ojo con el self-invocation: update() y delete() la llaman con
     * this.revertirEfectos(...) desde dentro de su propio cuerpo, y eso no pasa
     * por el proxy, asi que el evict de esta anotacion NO se dispara en esos dos
     * caminos. Por eso update() y delete() llevan su propio evict.
     * Esta anotacion cubre el unico llamador externo: ApelacionService, cuando
     * aprueba una apelacion.
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    public void revertirEfectos(Sancion sancion) {
        if (!Boolean.TRUE.equals(sancion.getEfectosAplicados())) {
            return;
        }
        efectos.de(sancion.getTipo()).ifPresent(efecto -> efecto.revertir(sancion));
        sancion.setEfectosAplicados(false);
        sancionRepository.save(sancion);
        reevaluarLogros(sancion);
    }

    /**
     * Una sancion mueve Elo o reclasifica la carrera, y eso puede completar
     * logros: del sancionado y, si cambiaron puestos, de los que subieron.
     * Perderlos no, porque evaluarLogros no quita un logro ya obtenido.
     */
    private void reevaluarLogros(Sancion sancion) {
        java.util.Map<Long, Usuario> afectados = new java.util.LinkedHashMap<>();
        afectados.put(sancion.getUsuario().getId(), sancion.getUsuario());
        boolean reclasifica = efectos.de(sancion.getTipo())
                .map(EfectoSancion::reclasificaCarrera).orElse(false);
        if (reclasifica && sancion.getCarrera() != null) {
            for (ResultadoCarrera r : resultadoCarreraRepository
                    .findByCarrera_IdOrderByPosicionFinalAsc(sancion.getCarrera().getId())) {
                afectados.putIfAbsent(r.getUsuario().getId(), r.getUsuario());
            }
        }
        afectados.values().forEach(logroService::evaluarLogros);
    }

    private void notificar(Sancion sancion) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(sancion.getUsuario())
                .tipo(TipoNotificacion.PENALIZACION)
                .mensaje("Recibiste una sancion: " + sancion.getTipo()
                        + (sancion.getMotivo() != null ? " - " + sancion.getMotivo() : ""))
                .leida(false)
                .build());
    }

    private SancionResponse toResponse(Sancion sancion) {
        var c = EntityMapper.resolveCarreraInfo(sancion.getCarrera());
        return new SancionResponse(
                sancion.getId(),
                sancion.getUsuario().getId(),
                c != null ? c.id() : null,
                c != null ? c.nombre() : null,
                c != null ? c.categoriaNombre() : null,
                sancion.getResolucion() != null ? sancion.getResolucion().getId() : null,
                sancion.getTipo(),
                sancion.getValor(),
                sancion.getMotivo(),
                sancion.getOrigen(),
                sancion.getIdExterno(),
                sancion.getFecha());
    }
}
