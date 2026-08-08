package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionProcesadaResponse;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionProcesadaService {

    private final SesionProcesadaRepository sesionProcesadaRepository;

    public SesionProcesada getEntity(Long id) {
        return sesionProcesadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion procesada no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SesionProcesadaResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SesionProcesadaResponse> listAll() {
        return sesionProcesadaRepository.findAllByOrderByFechaProcesamientoDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SesionProcesadaResponse> listarPorCarrera(Long carreraId) {
        return sesionProcesadaRepository.findByCarrera_IdOrderByFechaProcesamientoDesc(carreraId).stream()
                .map(this::toResponse).toList();
    }

    private SesionProcesadaResponse toResponse(SesionProcesada sesionProcesada) {
        return new SesionProcesadaResponse(
                sesionProcesada.getId(),
                sesionProcesada.getCarrera().getId(),
                sesionProcesada.getNombreArchivo(),
                sesionProcesada.getTipo(),
                sesionProcesada.getFechaProcesamiento());
    }
}
