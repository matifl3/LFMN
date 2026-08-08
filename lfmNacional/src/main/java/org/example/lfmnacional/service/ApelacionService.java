package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.apelacion.ApelacionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResolucionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResponse;
import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.entity.Notificacion;
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
    private final UsuarioService usuarioService;
    private final NotificacionRepository notificacionRepository;

    public Apelacion getEntity(Long id) {
        return apelacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apelacion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public ApelacionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listAll() {
        return apelacionRepository.findAllByOrderByFechaDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listarPendientes() {
        return apelacionRepository.findByEstadoOrderByFechaAsc(EstadoApelacion.PENDIENTE).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionResponse> listarPorUsuario(Long usuarioId) {
        return apelacionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ApelacionResponse create(ApelacionRequest request) {
        if (apelacionRepository.existsBySancion_IdAndUsuario_Id(request.sancionId(), request.usuarioId())) {
            throw new BusinessException("Ya existe una apelacion para esta sancion");
        }
        Apelacion apelacion = Apelacion.builder()
                .sancion(sancionService.getEntity(request.sancionId()))
                .usuario(usuarioService.getEntity(request.usuarioId()))
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
                apelacion.getMotivo(),
                apelacion.getEstado(),
                apelacion.getRespuestaAdmin(),
                apelacion.getFecha());
    }
}
