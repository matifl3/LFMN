package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.ResolucionResponse;
import org.example.lfmnacional.dto.incidente.ResolucionUpdateRequest;
import org.example.lfmnacional.entity.ResolucionIncidente;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ResolucionIncidenteRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolucionIncidenteService {

    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final SancionRepository sancionRepository;

    public ResolucionIncidente getEntity(Long id) {
        return resolucionIncidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resolucion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public ResolucionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ResolucionResponse> listAll() {
        return resolucionIncidenteRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResolucionResponse> listarPorComisario(Long comisarioId) {
        return resolucionIncidenteRepository.findByComisario_Id(comisarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public ResolucionResponse updateExplicacion(Long id, ResolucionUpdateRequest request) {
        ResolucionIncidente resolucion = getEntity(id);
        resolucion.setExplicacion(request.explicacion());
        return toResponse(resolucionIncidenteRepository.save(resolucion));
    }

    @Transactional
    public void delete(Long id) {
        ResolucionIncidente resolucion = getEntity(id);
        if (!sancionRepository.findByResolucion_Id(resolucion.getId()).isEmpty()) {
            throw new BusinessException("No se puede eliminar la resolucion porque tiene sanciones asociadas");
        }
        resolucionIncidenteRepository.delete(resolucion);
    }

    private ResolucionResponse toResponse(ResolucionIncidente resolucion) {
        return new ResolucionResponse(
                resolucion.getId(),
                resolucion.getIncidente().getId(),
                resolucion.getComisario().getId(),
                resolucion.getExplicacion(),
                resolucion.getFecha());
    }
}
