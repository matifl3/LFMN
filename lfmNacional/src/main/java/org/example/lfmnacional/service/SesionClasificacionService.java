package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionRequest;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionResponse;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionClasificacionService {

    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final CarreraService carreraService;
    private final UsuarioService usuarioService;

    public SesionClasificacion getEntity(Long id) {
        return sesionClasificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion de clasificacion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SesionClasificacionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SesionClasificacionResponse> listAll() {
        return sesionClasificacionRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SesionClasificacionResponse> listarPorCarrera(Long carreraId) {
        return sesionClasificacionRepository.findByCarrera_IdOrderByTiempoAsc(carreraId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SesionClasificacionResponse> listarPorUsuario(Long usuarioId) {
        return sesionClasificacionRepository.findByUsuario_IdOrderByTiempoAsc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public SesionClasificacionResponse create(SesionClasificacionRequest request) {
        SesionClasificacion clasificacion = SesionClasificacion.builder()
                .carrera(carreraService.getEntity(request.carreraId()))
                .usuario(usuarioService.getEntity(request.usuarioId()))
                .fecha(request.fecha() != null ? request.fecha() : LocalDateTime.now())
                .tiempo(request.tiempo())
                .diferenciaPole(request.diferenciaPole())
                .modeloAuto(request.modeloAuto())
                .skinAuto(request.skinAuto())
                .build();
        return toResponse(sesionClasificacionRepository.save(clasificacion));
    }

    @Transactional
    public SesionClasificacionResponse update(Long id, SesionClasificacionRequest request) {
        SesionClasificacion clasificacion = getEntity(id);
        clasificacion.setCarrera(carreraService.getEntity(request.carreraId()));
        clasificacion.setUsuario(usuarioService.getEntity(request.usuarioId()));
        clasificacion.setFecha(request.fecha() != null ? request.fecha() : clasificacion.getFecha());
        clasificacion.setTiempo(request.tiempo());
        clasificacion.setDiferenciaPole(request.diferenciaPole());
        clasificacion.setModeloAuto(request.modeloAuto());
        clasificacion.setSkinAuto(request.skinAuto());
        return toResponse(sesionClasificacionRepository.save(clasificacion));
    }

    @Transactional
    public void delete(Long id) {
        sesionClasificacionRepository.delete(getEntity(id));
    }

    private SesionClasificacionResponse toResponse(SesionClasificacion clasificacion) {
        return new SesionClasificacionResponse(
                clasificacion.getId(),
                clasificacion.getCarrera().getId(),
                clasificacion.getUsuario().getId(),
                clasificacion.getUsuario().getNombrePiloto(),
                clasificacion.getFecha(),
                clasificacion.getTiempo(),
                clasificacion.getDiferenciaPole(),
                clasificacion.getModeloAuto(),
                clasificacion.getSkinAuto());
    }
}
