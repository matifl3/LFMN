package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupCalificacionRequest;
import org.example.lfmnacional.dto.setup.SetupCalificacionResponse;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.entity.SetupCalificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetupCalificacionService {

    private final SetupCalificacionRepository setupCalificacionRepository;
    private final SetupService setupService;

    public SetupCalificacion getEntity(Long id) {
        return setupCalificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calificacion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SetupCalificacionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SetupCalificacionResponse> listarPorSetup(Long setupId) {
        return setupCalificacionRepository.findBySetup_Id(setupId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public SetupCalificacionResponse calificar(Long setupId, SetupCalificacionRequest request, Usuario usuario) {
        Setup setup = setupService.getEntity(setupId);
        SetupCalificacion calificacion = setupCalificacionRepository
                .findBySetup_IdAndUsuario_Id(setupId, usuario.getId())
                .orElseGet(() -> SetupCalificacion.builder()
                        .setup(setup)
                        .usuario(usuario)
                        .build());
        calificacion.setPuntaje(request.puntaje());
        SetupCalificacion guardada = setupCalificacionRepository.save(calificacion);
        setupService.recalcularPromedio(setup);
        return toResponse(guardada);
    }

    @Transactional
    public void delete(Long id, Usuario actual) {
        SetupCalificacion calificacion = getEntity(id);
        if (!calificacion.getUsuario().getId().equals(actual.getId()) && !actual.getRol().equals(Rol.ADMIN)) {
            throw new BusinessException("No tenes permiso para borrar esta calificacion");
        }
        Setup setup = calificacion.getSetup();
        setupCalificacionRepository.delete(calificacion);
        setupService.recalcularPromedio(setup);
    }

    private SetupCalificacionResponse toResponse(SetupCalificacion calificacion) {
        return new SetupCalificacionResponse(
                calificacion.getId(),
                calificacion.getSetup().getId(),
                calificacion.getUsuario().getId(),
                calificacion.getPuntaje());
    }
}
