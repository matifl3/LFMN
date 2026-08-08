package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.notificacion.NotificacionRequest;
import org.example.lfmnacional.dto.notificacion.NotificacionResponse;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioService usuarioService;

    public Notificacion getEntity(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public NotificacionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaDesc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuario_IdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public NotificacionResponse create(NotificacionRequest request) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuarioService.getEntity(request.usuarioId()))
                .tipo(request.tipo())
                .mensaje(request.mensaje())
                .leida(false)
                .link(request.link())
                .build();
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse marcarLeida(Long id) {
        Notificacion notificacion = getEntity(id);
        notificacion.setLeida(true);
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuario_IdAndLeidaFalseOrderByFechaDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
    }

    @Transactional
    public void delete(Long id) {
        notificacionRepository.delete(getEntity(id));
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getUsuario().getId(),
                notificacion.getTipo(),
                notificacion.getMensaje(),
                notificacion.getLeida(),
                notificacion.getFecha(),
                notificacion.getLink());
    }
}
