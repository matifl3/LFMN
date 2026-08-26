package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private static final int MINUTOS_CIERRE_PREVIO = 5;

    private final CarreraRepository carreraRepository;
    private final CampeonatoService campeonatoService;
    private final ArchivoCarreraService archivoCarreraService;
    private final InscripcionRepository inscripcionRepository;

    public Carrera getEntity(Long id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public CarreraResponse getById(Long id) {
        return toResponse(getEntity(id), null);
    }

    @Transactional(readOnly = true)
    public Page<CarreraResponse> listAll(Pageable pageable) {
        Page<Carrera> carreras = carreraRepository.findAll(pageable);
        Map<Long, Long> counts = countInscriptos();
        return carreras.map(c -> toResponse(c, counts));
    }

    @Transactional(readOnly = true)
    @Cacheable("carreras_proximas")
    public List<CarreraResponse> proximas() {
        List<Carrera> carreras = carreraRepository.findByFechaAfterOrderByFechaAsc(LocalDateTime.now());
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts)).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable("carreras_pasadas")
    public List<CarreraResponse> pasadas() {
        List<Carrera> carreras = carreraRepository.findByFechaBeforeOrderByFechaDesc(LocalDateTime.now());
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts)).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> porCampeonato(Long campeonatoId) {
        List<Carrera> carreras = carreraRepository.findByCampeonato_IdOrderByFechaDesc(campeonatoId);
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts)).toList();
    }

    @Transactional
    @CacheEvict(value = {"carreras_proximas", "carreras_pasadas"}, allEntries = true)
    public CarreraResponse create(CarreraRequest request) {
        Carrera carrera = Carrera.builder()
                .nombre(request.nombre())
                .fecha(request.fecha())
                .circuito(request.circuito())
                .campeonato(campeonatoService.getEntity(request.campeonatoId()))
                .estado(request.estado())
                .cupoMaximo(request.cupoMaximo())
                .servidor(request.servidor())
                .contrasenaServidor(request.contrasenaServidor())
                .archivo(request.archivoId() != null ? archivoCarreraService.getEntity(request.archivoId()) : null)
                .linkPista(request.linkPista())
                .linkAuto(request.linkAuto())
                .build();
        return toResponse(carreraRepository.save(carrera), null);
    }

    @Transactional
    @CacheEvict(value = {"carreras_proximas", "carreras_pasadas"}, allEntries = true)
    public CarreraResponse update(Long id, CarreraRequest request) {
        Carrera carrera = getEntity(id);
        carrera.setNombre(request.nombre());
        carrera.setFecha(request.fecha());
        carrera.setCircuito(request.circuito());
        carrera.setCampeonato(campeonatoService.getEntity(request.campeonatoId()));
        carrera.setEstado(request.estado() != null ? request.estado() : carrera.getEstado());
        carrera.setCupoMaximo(request.cupoMaximo());
        carrera.setServidor(request.servidor());
        carrera.setContrasenaServidor(request.contrasenaServidor());
        if (request.archivoId() != null) {
            carrera.setArchivo(archivoCarreraService.getEntity(request.archivoId()));
        }
        carrera.setLinkPista(request.linkPista());
        carrera.setLinkAuto(request.linkAuto());
        return toResponse(carreraRepository.save(carrera), null);
    }

    @Transactional
    public CarreraResponse vincularArchivo(Long id, Long archivoId) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(archivoCarreraService.getEntity(archivoId));
        return toResponse(carreraRepository.save(carrera), null);
    }

    @Transactional
    public CarreraResponse desvincularArchivo(Long id) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(null);
        return toResponse(carreraRepository.save(carrera), null);
    }

    @Transactional
    @CacheEvict(value = {"carreras_proximas", "carreras_pasadas"}, allEntries = true)
    public CarreraResponse changeEstado(Long id, EstadoCarrera estado) {
        Carrera carrera = getEntity(id);
        carrera.setEstado(estado);
        return toResponse(carreraRepository.save(carrera), null);
    }

    @Transactional
    public CarreraResponse cancelar(Long id) {
        return changeEstado(id, EstadoCarrera.CANCELADA);
    }

    @Transactional
    @CacheEvict(value = {"carreras_proximas", "carreras_pasadas"}, allEntries = true)
    public void delete(Long id) {
        Carrera carrera = getEntity(id);
        carreraRepository.delete(carrera);
    }

    @Transactional
    public void cerrarInscripcionesAutomaticamente() {
        LocalDateTime limite = LocalDateTime.now().plusMinutes(MINUTOS_CIERRE_PREVIO);
        List<EstadoCarrera> abiertas = List.of(EstadoCarrera.PROGRAMADA, EstadoCarrera.INSCRIPCIONES_ABIERTAS);
        List<Carrera> porCerrar = carreraRepository.findByEstadoInAndFechaBefore(abiertas, limite);
        for (Carrera carrera : porCerrar) {
            carrera.setEstado(EstadoCarrera.INSCRIPCIONES_CERRADAS);
        }
        if (!porCerrar.isEmpty()) {
            carreraRepository.saveAll(porCerrar);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduledCierreInscripciones() {
        cerrarInscripcionesAutomaticamente();
    }

    private Map<Long, Long> countInscriptos() {
        return inscripcionRepository.countInscriptosPorCarreraRaw().stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private CarreraResponse toResponse(Carrera carrera, Map<Long, Long> counts) {
        Long inscritos = counts != null ? counts.getOrDefault(carrera.getId(), 0L) : null;
        return new CarreraResponse(
                carrera.getId(),
                carrera.getNombre(),
                carrera.getFecha(),
                carrera.getCircuito(),
                carrera.getCampeonato().getId(),
                carrera.getCampeonato().getNombre(),
                carrera.getCampeonato().getCategoria().getId(),
                carrera.getCampeonato().getCategoria().getNombre(),
                carrera.getEstado(),
                carrera.getCupoMaximo(),
                inscritos,
                carrera.getServidor(),
                carrera.getArchivo() != null ? carrera.getArchivo().getId() : null,
                carrera.getArchivo() != null ? carrera.getArchivo().getNombre() : null,
                carrera.getLinkPista(),
                carrera.getLinkAuto());
    }
}
