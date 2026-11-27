package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.*;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.ResolucionIncidente;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VotoComisario;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.EntityMapper;
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

    /**
     * Votos (de cualquier signo) que tienen que existir antes de poder redactar la
     * resolucion. Antes eran "votos A_FAVOR" y al llegar a 2 el incidente se
     * cerraba solo, sin resolucion ni sanciones: el formulario de resolver
     * desaparecia justo cuando habia acuerdo.
     */
    static final int QUORUM_VOTOS = 2;

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
                .fecha(LocalDateTime.now())
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

    /**
     * Cierra el incidente: exige quorum de votos, una sola resolucion por
     * incidente y aplica las sanciones en la misma transaccion (si una falla, no
     * queda ni la resolucion ni el estado RESUELTO). El comisario es siempre el
     * usuario autenticado, nunca un id que venga en el body.
     */
    @Transactional
    public ResolucionResponse guardarResolucion(Long incidenteId, ResolucionRequest request, Usuario comisario) {
        Incidente incidente = getEntity(incidenteId);
        if (incidente.getEstado() == EstadoIncidente.RESUELTO) {
            throw new BusinessException("El incidente ya esta resuelto");
        }
        if (resolucionIncidenteRepository.findByIncidente_Id(incidenteId).isPresent()) {
            throw new BusinessException("El incidente ya tiene una resolucion");
        }
        long votos = votoComisarioRepository.findByIncidente_Id(incidenteId).size();
        if (votos < QUORUM_VOTOS) {
            throw new BusinessException("Faltan votos: se necesitan " + QUORUM_VOTOS
                    + " para resolver y hay " + votos);
        }
        ResolucionIncidente resolucion = ResolucionIncidente.builder()
                .incidente(incidente)
                .comisario(comisario)
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
                        OrigenSancion.COMISARIO,
                        sancion.idExterno(),
                        sancion.fecha());
                sancionService.create(conResolucion);
            }
        }

        incidente.setEstado(EstadoIncidente.RESUELTO);
        incidenteRepository.save(incidente);
        return toResolucionResponse(resolucion);
    }

    /**
     * Registra (o cambia) el voto del comisario autenticado. El primer voto pasa
     * el incidente a EN_ANALISIS; cerrarlo es trabajo de {@link #guardarResolucion}.
     */
    @Transactional
    public VotoResponse votar(Long incidenteId, VotoRequest request, Usuario comisario) {
        Incidente incidente = getEntity(incidenteId);
        if (incidente.getEstado() == EstadoIncidente.RESUELTO) {
            throw new BusinessException("El incidente ya esta resuelto y no acepta votos");
        }
        VotoComisario voto = votoComisarioRepository
                .findByIncidente_IdAndComisario_Id(incidenteId, comisario.getId())
                .orElseGet(() -> VotoComisario.builder()
                        .incidente(incidente)
                        .comisario(comisario)
                        .build());
        voto.setDecision(request.decision());
        voto.setComentario(request.comentario());
        voto.setFecha(LocalDateTime.now());
        voto = votoComisarioRepository.save(voto);

        if (incidente.getEstado() == EstadoIncidente.PENDIENTE) {
            incidente.setEstado(EstadoIncidente.EN_ANALISIS);
            incidenteRepository.save(incidente);
        }
        return new VotoResponse(
                voto.getId(),
                voto.getIncidente().getId(),
                voto.getComisario().getId(),
                voto.getDecision(),
                voto.getComentario(),
                voto.getFecha());
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
        var c = EntityMapper.resolveCarreraInfo(incidente.getCarrera());
        var u = EntityMapper.resolveUsuarioBasico(incidente.getReportante());
        return new IncidenteResponse(
                incidente.getId(),
                c.id(),
                c.nombre(),
                c.categoriaNombre(),
                u.id(),
                u.nombrePiloto(),
                incidente.getVuelta(),
                incidente.getDescripcion(),
                incidente.getVideoUrl(),
                incidente.getFecha(),
                incidente.getEstado(),
                QUORUM_VOTOS);
    }
}
