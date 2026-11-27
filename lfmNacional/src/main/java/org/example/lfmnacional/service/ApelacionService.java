package org.example.lfmnacional.service;

import org.example.lfmnacional.exception.PermisoDenegadoException;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.apelacion.ApelacionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResolucionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResponse;
import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoApelacion;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApelacionService {

    private final ApelacionRepository apelacionRepository;
    private final SancionService sancionService;
    private final NotificacionRepository notificacionRepository;
    private final CampeonatoAccesoService accesoService;

    public Apelacion getEntity(Long id) {
        return apelacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apelacion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public ApelacionResponse getById(Long id, Usuario visor) {
        Apelacion apelacion = getEntity(id);
        accesoService.exigirVeCarreraSiHay(visor, apelacion.getSancion().getCarrera());
        return toResponse(apelacion);
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listAll(Usuario visor) {
        return visibles(apelacionRepository.findAllByOrderByFechaDesc(), visor);
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listarPendientes(Usuario visor) {
        return visibles(apelacionRepository.findByEstadoOrderByFechaAsc(EstadoApelacion.PENDIENTE), visor);
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listarPorUsuario(Long usuarioId, Usuario visor) {
        return visibles(apelacionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId), visor);
    }

    private List<ApelacionResponse> visibles(List<Apelacion> apelaciones, Usuario visor) {
        var ve = accesoService.filtroVeCarrera(visor);
        return apelaciones.stream()
                .filter(a -> ve.test(a.getSancion().getCarrera()))
                .map(this::toResponse).toList();
    }

    @Transactional
    public ApelacionResponse create(ApelacionRequest request, Usuario usuario) {
        Sancion sancion = sancionService.getEntity(request.sancionId());
        // Solo el sancionado puede apelar su propia sancion.
        if (!sancion.getUsuario().getId().equals(usuario.getId())) {
            throw new PermisoDenegadoException("Solo podes apelar tus propias sanciones");
        }
        if (apelacionRepository.existsBySancion_IdAndUsuario_Id(request.sancionId(), usuario.getId())) {
            throw new BusinessException("Ya existe una apelacion para esta sancion");
        }
        Apelacion apelacion = Apelacion.builder()
                .sancion(sancion)
                .usuario(usuario)
                .motivo(request.motivo())
                .build();
        return toResponse(apelacionRepository.save(apelacion));
    }

    @Transactional
    public ApelacionResponse resolver(Long id, ApelacionResolucionRequest request) {
        Apelacion apelacion = getEntity(id);
        if (request.estado() == EstadoApelacion.PENDIENTE) {
            throw new BusinessException("El estado debe ser APROBADA o RECHAZADA");
        }
        if (apelacion.getEstado() != EstadoApelacion.PENDIENTE) {
            throw new BusinessException("La apelacion ya fue resuelta");
        }
        apelacion.setEstado(request.estado());
        apelacion.setRespuestaAdmin(request.respuestaAdmin());
        if (request.estado() == EstadoApelacion.APROBADA) {
            sancionService.revertirEfectos(apelacion.getSancion());
        }
        notificar(apelacion);
        return toResponse(apelacionRepository.save(apelacion));
    }

    @Transactional
    public void delete(Long id) {
        apelacionRepository.delete(getEntity(id));
    }

    private void notificar(Apelacion apelacion) {
        String mensaje = apelacion.getEstado() == EstadoApelacion.APROBADA
                ? "Tu apelacion fue aprobada: se revirtieron los efectos de la sancion"
                : "Tu apelacion fue rechazada";
        if (apelacion.getRespuestaAdmin() != null && !apelacion.getRespuestaAdmin().isBlank()) {
            mensaje += ": " + apelacion.getRespuestaAdmin();
        }
        notificacionRepository.save(Notificacion.builder()
                .usuario(apelacion.getUsuario())
                .tipo(TipoNotificacion.APELACION)
                .mensaje(mensaje)
                .leida(false)
                .build());
    }

    private ApelacionResponse toResponse(Apelacion apelacion) {
        return new ApelacionResponse(
                apelacion.getId(),
                apelacion.getSancion().getId(),
                apelacion.getUsuario().getId(),
                apelacion.getUsuario().getNombrePiloto(),
                apelacion.getMotivo(),
                apelacion.getEstado(),
                apelacion.getRespuestaAdmin(),
                apelacion.getFecha());
    }
}
