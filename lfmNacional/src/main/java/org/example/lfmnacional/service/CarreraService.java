package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private static final int MINUTOS_CIERRE_PREVIO = 5;

    private final CarreraRepository carreraRepository;
    private final CampeonatoService campeonatoService;
    private final ArchivoCarreraService archivoCarreraService;
    private final InscripcionRepository inscripcionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final IncidenteRepository incidenteRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final SesionProcesadaRepository sesionProcesadaRepository;
    private final SancionRepository sancionRepository;

    public Carrera getEntity(Long id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public CarreraResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> listAll() {
        return carreraRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> proximas() {
        return carreraRepository.findByFechaAfterOrderByFechaAsc(LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> pasadas() {
        return carreraRepository.findByFechaBeforeOrderByFechaDesc(LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> porCampeonato(Long campeonatoId) {
        return carreraRepository.findByCampeonato_IdOrderByFechaDesc(campeonatoId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
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
                .build();
        return toResponse(carreraRepository.save(carrera));
    }

    @Transactional
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
        return toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraResponse vincularArchivo(Long id, Long archivoId) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(archivoCarreraService.getEntity(archivoId));
        return toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraResponse desvincularArchivo(Long id) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(null);
        return toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraResponse changeEstado(Long id, EstadoCarrera estado) {
        Carrera carrera = getEntity(id);
        carrera.setEstado(estado);
        return toResponse(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraResponse cancelar(Long id) {
        return changeEstado(id, EstadoCarrera.CANCELADA);
    }

    @Transactional
    public void delete(Long id) {
        Carrera carrera = getEntity(id);
        List<String> dependencias = new ArrayList<>();
        if (inscripcionRepository.existsByCarrera_Id(id)) {
            dependencias.add("inscripciones");
        }
        if (resultadoCarreraRepository.existsByCarrera_Id(id)) {
            dependencias.add("resultados");
        }
        if (incidenteRepository.existsByCarrera_Id(id)) {
            dependencias.add("incidentes");
        }
        if (sesionClasificacionRepository.existsByCarrera_Id(id)) {
            dependencias.add("sesiones de clasificacion");
        }
        if (sesionProcesadaRepository.existsByCarrera_Id(id)) {
            dependencias.add("sesiones procesadas");
        }
        if (sancionRepository.existsByCarrera_Id(id)) {
            dependencias.add("sanciones");
        }
        if (!dependencias.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar la carrera '" + carrera.getNombre()
                            + "' porque tiene " + String.join(", ", dependencias) + " asociadas");
        }
        carreraRepository.delete(carrera);
    }

    @Transactional
    public void cerrarInscripcionesAutomaticamente() {
        LocalDateTime limite = LocalDateTime.now().plusMinutes(MINUTOS_CIERRE_PREVIO);
        List<EstadoCarrera> abiertas = List.of(EstadoCarrera.PROGRAMADA, EstadoCarrera.INSCRIPCIONES_ABIERTAS);
        List<Carrera> porCerrar = new ArrayList<>();
        for (EstadoCarrera estado : abiertas) {
            carreraRepository.findByEstado(estado).stream()
                    .filter(carrera -> carrera.getFecha() != null && !carrera.getFecha().isAfter(limite))
                    .forEach(porCerrar::add);
        }
        for (Carrera carrera : porCerrar) {
            carrera.setEstado(EstadoCarrera.INSCRIPCIONES_CERRADAS);
            carreraRepository.save(carrera);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduledCierreInscripciones() {
        cerrarInscripcionesAutomaticamente();
    }

    private CarreraResponse toResponse(Carrera carrera) {
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
                carrera.getServidor(),
                carrera.getContrasenaServidor(),
                carrera.getArchivo() != null ? carrera.getArchivo().getId() : null,
                carrera.getArchivo() != null ? carrera.getArchivo().getNombre() : null);
    }
}
