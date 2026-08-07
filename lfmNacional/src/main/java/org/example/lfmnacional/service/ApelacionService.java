package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.apelacion.ApelacionResolucionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResponse;
import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.enums.EstadoApelacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApelacionService {

    private final ApelacionRepository apelacionRepository;

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
    public ApelacionResponse resolver(Long id, ApelacionResolucionRequest request) {
        Apelacion apelacion = getEntity(id);
        if (request.estado() == EstadoApelacion.PENDIENTE) {
            throw new BusinessException("El estado debe ser APROBADA o RECHAZADA");
        }
        apelacion.setEstado(request.estado());
        apelacion.setRespuestaAdmin(request.respuestaAdmin());
        return toResponse(apelacionRepository.save(apelacion));
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
