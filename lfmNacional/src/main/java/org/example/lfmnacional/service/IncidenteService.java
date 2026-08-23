package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.*;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.ResolucionIncidente;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VotoComisario;
import org.example.lfmnacional.enums.DecisionComisario;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.ResolucionIncidenteRepository;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class IncidenteService {

    private static final int QUORUM_A_FAVOR = 2;

    private final IncidenteRepository incidenteRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final VotoComisarioRepository votoComisarioRepository;
    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final CarreraService carreraService;
    private final UsuarioService usuarioService;
    private final SancionService sancionService;

    public Incidente getEntity(Long id) {
        return incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public IncidenteResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<IncidenteResponse> listAll(Pageable pageable) {
        return incidenteRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorEstado(EstadoIncidente estado) {
        return incidenteRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorCarrera(Long carreraId) {
        return incidenteRepository.findByCarrera_Id(carreraId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorReportante(Long reportanteId) {
        return incidenteRepository.findByReportante_Id(reportanteId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public IncidenteResponse reportar(IncidenteRequest request, Usuario reportante) {
        Incidente incidente = Incidente.builder()
                .carrera(carreraService.getEntity(request.carreraId()))
                .reportante(reportante)
                .vuelta(request.vuelta())
                .descripcion(request.descripcion())
                .videoUrl(request.videoUrl())
                .estado(EstadoIncidente.PENDIENTE)
                .build();
        incidente = incidenteRepository.save(incidente);
        incidentePilotoRepository.save(IncidentePiloto.builder()
                .incidente(incidente)
                .usuario(incidente.getReportante())
                .rol(RolPilotoIncidente.AFECTADO)
                .build());
        return toResponse(incidente);
    }

    @Transactional
    public List<IncidentePilotoResponse> asignarPilotos(Long incidenteId, List<IncidentePilotoRequest> pilotos) {
        Incidente incidente = getEntity(incidenteId);
        Map<Long, IncidentePiloto> existentes = incidentePilotoRepository.findByIncidente_Id(incidenteId).stream()
                .collect(Collectors.toMap(ip -> ip.getUsuario().getId(), ip -> ip));
        for (IncidentePilotoRequest request : pilotos) {
            IncidentePiloto existing = existentes.get(request.usuarioId());
            if (existing != null) {
                existing.setRol(request.rol());
                incidentePilotoRepository.save(existing);
            } else {
                IncidentePiloto nuevo = IncidentePiloto.builder()
                        .incidente(incidente)
                        .usuario(usuarioService.getEntity(request.usuarioId()))
                        .rol(request.rol())
                        .build();
                incidentePilotoRepository.save(nuevo);
                existentes.put(request.usuarioId(), nuevo);
            }
        }
        return listarPilotos(incidenteId);
    }

    @Transactional(readOnly = true)
    public List<IncidentePilotoResponse> listarPilotos(Long incidenteId) {
        return incidentePilotoRepository.findByIncidente_Id(incidenteId).stream()
                .map(piloto -> new IncidentePilotoResponse(
                        piloto.getId(),
                        piloto.getIncidente().getId(),
                        piloto.getUsuario().getId(),
                        piloto.getUsuario().getNombrePiloto(),
                        piloto.getRol()))
                .toList();
    }

    @Transactional
    public ResolucionResponse guardarResolucion(Long incidenteId, ResolucionRequest request) {
        Incidente incidente = getEntity(incidenteId);
        if (incidente.getEstado() == EstadoIncidente.RESUELTO) {
            throw new BusinessException("El incidente ya esta resuelto");
        }
        ResolucionIncidente resolucion = ResolucionIncidente.builder()
                .incidente(incidente)
                .comisario(usuarioService.getEntity(request.comisarioId()))
                .explicacion(request.explicacion())
                .build();
        resolucion = resolucionIncidenteRepository.save(resolucion);

        if (request.sanciones() != null) {
            for (SancionRequest sancion : request.sanciones()) {
                SancionRequest conResolucion = new SancionRequest(
                        sancion.usuarioId(),
                        sancion.carreraId() != null ? sancion.carreraId() : incidente.getCarrera().getId(),
                        resolucion.getId(),
                        sancion.tipo(),
                        sancion.valor(),
                        sancion.motivo(),
                        sancion.origen(),
                        sancion.idExterno(),
                        sancion.fecha());
                sancionService.create(conResolucion);
            }
        }

        if (incidente.getEstado() == EstadoIncidente.PENDIENTE) {
            incidente.setEstado(EstadoIncidente.EN_ANALISIS);
            incidenteRepository.save(incidente);
        }
        return toResolucionResponse(resolucion);
    }

    @Transactional
    public VotoResponse votar(Long incidenteId, VotoRequest request) {
        Incidente incidente = getEntity(incidenteId);
        if (incidente.getEstado() == EstadoIncidente.RESUELTO) {
            throw new BusinessException("El incidente ya esta resuelto y no acepta votos");
        }
        VotoComisario voto = votoComisarioRepository
                .findByIncidente_IdAndComisario_Id(incidenteId, request.comisarioId())
                .orElseGet(() -> VotoComisario.builder()
                        .incidente(incidente)
                        .comisario(usuarioService.getEntity(request.comisarioId()))
                        .build());
        voto.setDecision(request.decision());
        voto.setComentario(request.comentario());
        voto.setFecha(LocalDateTime.now());
        voto = votoComisarioRepository.save(voto);

        verificarQuorum(incidenteId);
        return new VotoResponse(
                voto.getId(),
                voto.getIncidente().getId(),
                voto.getComisario().getId(),
                voto.getDecision(),
                voto.getComentario(),
                voto.getFecha());
    }

    private void verificarQuorum(Long incidenteId) {
        Incidente incidente = getEntity(incidenteId);
        long aFavor = votoComisarioRepository.findByIncidente_Id(incidenteId).stream()
                .filter(v -> v.getDecision() == DecisionComisario.A_FAVOR)
                .count();
        if (aFavor >= QUORUM_A_FAVOR && incidente.getEstado() != EstadoIncidente.RESUELTO) {
            incidente.setEstado(EstadoIncidente.RESUELTO);
            incidenteRepository.save(incidente);
        }
    }

    @Transactional(readOnly = true)
    public ResolucionResponse getResolucion(Long incidenteId) {
        ResolucionIncidente resolucion = resolucionIncidenteRepository.findByIncidente_Id(incidenteId)
                .orElseThrow(() -> new ResourceNotFoundException("El incidente no tiene resolucion"));
        return toResolucionResponse(resolucion);
    }

    @Transactional(readOnly = true)
    public List<DecisionComisarioResponse> listarDecisionesComisario(Long comisarioId) {
        List<DecisionComisarioResponse> votos = votoComisarioRepository
                .findByComisario_IdOrderByFechaDesc(comisarioId).stream()
                .map(voto -> new DecisionComisarioResponse(
                        voto.getIncidente().getId(),
                        voto.getDecision(),
                        voto.getComentario(),
                        voto.getFecha(),
                        "VOTO"))
                .toList();
        List<DecisionComisarioResponse> resoluciones = resolucionIncidenteRepository
                .findByComisario_Id(comisarioId).stream()
                .map(resolucion -> new DecisionComisarioResponse(
                        resolucion.getIncidente().getId(),
                        null,
                        resolucion.getExplicacion(),
                        resolucion.getFecha(),
                        "RESOLUCION"))
                .toList();
        return Stream.concat(votos.stream(), resoluciones.stream()).toList();
    }

    private ResolucionResponse toResolucionResponse(ResolucionIncidente resolucion) {
        return new ResolucionResponse(
                resolucion.getId(),
                resolucion.getIncidente().getId(),
                resolucion.getComisario().getId(),
                resolucion.getExplicacion(),
                resolucion.getFecha());
    }

    private IncidenteResponse toResponse(Incidente incidente) {
        return new IncidenteResponse(
                incidente.getId(),
                incidente.getCarrera().getId(),
                incidente.getCarrera().getNombre(),
                incidente.getCarrera().getCampeonato().getCategoria().getNombre(),
                incidente.getReportante().getId(),
                incidente.getReportante().getNombrePiloto(),
                incidente.getVuelta(),
                incidente.getDescripcion(),
                incidente.getVideoUrl(),
                incidente.getEstado());
    }
}
