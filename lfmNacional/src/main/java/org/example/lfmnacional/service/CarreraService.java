package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.carrera.CarreraAccesoResponse;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.dto.carrera.EloEstimadoResponse;
import org.example.lfmnacional.service.rating.EloCalculator;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
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
    private static final int MINUTOS_AVISO_INICIO = 30;

    private final CarreraRepository carreraRepository;
    private final CampeonatoService campeonatoService;
    private final CampeonatoAccesoService accesoService;
    private final ArchivoCarreraService archivoCarreraService;
    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;
    private final EloCalculator eloCalculator;

    public Carrera getEntity(Long id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public CarreraResponse getById(Long id, Usuario visor) {
        Carrera carrera = getEntity(id);
        accesoService.exigirVeCarrera(visor, carrera);
        return toResponse(carrera, null, false);
    }

    @Transactional(readOnly = true)
    public Page<CarreraResponse> listAll(Pageable pageable, Usuario visor) {
        Page<Carrera> carreras = carreraRepository.findAll(pageable);
        Map<Long, Long> counts = countInscriptos();
        return carreras.map(c -> toResponse(c, counts, !accesoService.veCarrera(visor, c)));
    }

    /**
     * El calendario es publico, asi que estas dos no se cachean: la respuesta se
     * poda segun quien pregunte y una cache compartida le serviria a un piloto
     * los links y el servidor de un campeonato privado del que no es miembro.
     */
    @Transactional(readOnly = true)
    public List<CarreraResponse> proximas(Usuario visor) {
        List<Carrera> carreras = carreraRepository.findByFechaAfterOrderByFechaAsc(LocalDateTime.now());
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts, !accesoService.veCarrera(visor, c))).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> pasadas(Usuario visor) {
        List<Carrera> carreras = carreraRepository.findByFechaBeforeOrderByFechaDesc(LocalDateTime.now());
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts, !accesoService.veCarrera(visor, c))).toList();
    }

    @Transactional(readOnly = true)
    public List<CarreraResponse> porCampeonato(Long campeonatoId, Usuario visor) {
        boolean ocultar = !accesoService.veContenido(visor, campeonatoService.getEntity(campeonatoId));
        List<Carrera> carreras = carreraRepository.findByCampeonato_IdOrderByFechaDesc(campeonatoId);
        Map<Long, Long> counts = countInscriptos();
        return carreras.stream().map(c -> toResponse(c, counts, ocultar)).toList();
    }

    @Transactional
    public CarreraResponse create(CarreraRequest request) {
        Carrera carrera = Carrera.builder()
                .nombre(request.nombre())
                .fecha(request.fecha())
                .practicaFecha(request.practicaFecha())
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
        return toResponse(carreraRepository.save(carrera), null, false);
    }

    @Transactional
    public CarreraResponse update(Long id, CarreraRequest request) {
        Carrera carrera = getEntity(id);
        LocalDateTime fechaAnterior = carrera.getFecha();
        carrera.setNombre(request.nombre());
        carrera.setFecha(request.fecha());
        carrera.setPracticaFecha(request.practicaFecha());
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
        CarreraResponse response = toResponse(carreraRepository.save(carrera), null, false);
        if (fechaAnterior != null && request.fecha() != null
                && request.fecha().isAfter(fechaAnterior)) {
            notificarInscriptos(carrera, "La carrera \"" + carrera.getNombre()
                    + "\" fue pospuesta. Nueva fecha: " + request.fecha() + ".");
        }
        return response;
    }

    @Transactional
    public CarreraResponse vincularArchivo(Long id, Long archivoId) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(archivoCarreraService.getEntity(archivoId));
        return toResponse(carreraRepository.save(carrera), null, false);
    }

    @Transactional
    public CarreraResponse desvincularArchivo(Long id) {
        Carrera carrera = getEntity(id);
        carrera.setArchivo(null);
        return toResponse(carreraRepository.save(carrera), null, false);
    }

    @Transactional
    public CarreraResponse changeEstado(Long id, EstadoCarrera estado) {
        Carrera carrera = getEntity(id);
        carrera.setEstado(estado);
        CarreraResponse response = toResponse(carreraRepository.save(carrera), null, false);
        if (estado == EstadoCarrera.CANCELADA) {
            notificarInscriptos(carrera, "La carrera \"" + carrera.getNombre()
                    + "\" fue cancelada. Cualquier inscripcion activa queda anulada.");
        } else if (estado == EstadoCarrera.EN_CURSO) {
            notificarInscriptos(carrera, "La carrera \"" + carrera.getNombre()
                    + "\" esta en curso. Ingresa al servidor asignado.");
        }
        return response;
    }

    @Transactional
    public CarreraResponse cancelar(Long id) {
        return changeEstado(id, EstadoCarrera.CANCELADA);
    }

    @Transactional
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

    @Transactional
    public void notificarCarrerasPorComenzar() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusMinutes(MINUTOS_AVISO_INICIO);
        List<EstadoCarrera> estados = List.of(EstadoCarrera.PROGRAMADA,
                EstadoCarrera.INSCRIPCIONES_ABIERTAS,
                EstadoCarrera.INSCRIPCIONES_CERRADAS);
        List<Carrera> proximas = carreraRepository.findByEstadoInAndFechaBetween(estados, ahora, limite);
        for (Carrera carrera : proximas) {
            String link = "/carreras/" + carrera.getId();
            if (notificacionRepository.existsByTipoAndLink(TipoNotificacion.CARRERA_INICIO, link)) {
                continue;
            }
            List<Inscripcion> inscripciones = inscripcionRepository.findByCarrera_IdAndEstado(
                    carrera.getId(), EstadoInscripcion.INSCRIPTO);
            for (Inscripcion inscripcion : inscripciones) {
                notificacionRepository.save(Notificacion.builder()
                        .usuario(inscripcion.getUsuario())
                        .tipo(TipoNotificacion.CARRERA_INICIO)
                        .mensaje("La carrera \"" + carrera.getNombre()
                                + "\" comienza en aproximadamente " + MINUTOS_AVISO_INICIO
                                + " minutos. Preparate para ingresar al servidor.")
                        .leida(false)
                        .link(link)
                        .build());
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduledNotificacionInicioCarreras() {
        notificarCarrerasPorComenzar();
    }

    private void notificarInscriptos(Carrera carrera, String mensaje) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByCarrera_Id(carrera.getId()).stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.INSCRIPTO
                        || i.getEstado() == EstadoInscripcion.LISTA_ESPERA)
                .toList();
        String link = "/carreras/" + carrera.getId();
        for (Inscripcion inscripcion : inscripciones) {
            notificacionRepository.save(Notificacion.builder()
                    .usuario(inscripcion.getUsuario())
                    .tipo(TipoNotificacion.CARRERA_ESTADO)
                    .mensaje(mensaje)
                    .leida(false)
                    .link(link)
                    .build());
        }
    }

    private Map<Long, Long> countInscriptos() {
        return inscripcionRepository.countInscriptosPorCarreraRaw().stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Transactional(readOnly = true)
    public CarreraAccesoResponse accesoServidor(Long carreraId, Long usuarioId) {
        Carrera carrera = getEntity(carreraId);
        if (usuarioId != null) {
            boolean inscripto = inscripcionRepository.findByCarrera_IdAndUsuario_Id(carreraId, usuarioId)
                    .map(i -> i.getEstado() == EstadoInscripcion.INSCRIPTO)
                    .orElse(false);
            if (!inscripto) {
                throw new BusinessException("La contrasena del servidor solo es visible para pilotos inscriptos");
            }
        } else {
            throw new BusinessException("Debes iniciar sesion para ver la contrasena del servidor");
        }
        return new CarreraAccesoResponse(carrera.getId(), carrera.getServidor(), carrera.getContrasenaServidor());
    }

    @Transactional(readOnly = true)
    public EloEstimadoResponse eloEstimado(Long carreraId, Long usuarioId) {
        if (usuarioId == null) {
            throw new BusinessException("Debes iniciar sesion para ver el Elo estimado");
        }
        Carrera carrera = getEntity(carreraId);
        List<Inscripcion> inscriptos = inscripcionRepository.findByCarrera_IdAndEstado(
                carreraId, EstadoInscripcion.INSCRIPTO);
        if (inscriptos.isEmpty()) {
            throw new BusinessException("La carrera no tiene pilotos inscriptos");
        }
        Inscripcion propia = inscriptos.stream()
                .filter(i -> i.getUsuario().getId().equals(usuarioId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Debes estar inscripto para ver el Elo estimado"));
        Integer eloPropio = propia.getUsuario().getElo() != null ? propia.getUsuario().getElo() : 1200;
        List<Integer> elosRivales = inscriptos.stream()
                .map(i -> i.getUsuario().getElo())
                .filter(e -> e != null)
                .toList();
        int total = inscriptos.size();
        int posicionEsperada = 1 + (int) inscriptos.stream()
                .filter(i -> !i.getUsuario().getId().equals(usuarioId))
                .filter(i -> (i.getUsuario().getElo() != null ? i.getUsuario().getElo() : 1200) > eloPropio)
                .count();
        List<EloEstimadoResponse.PosicionElo> detalle = new java.util.ArrayList<>();
        for (int pos = 1; pos <= total; pos++) {
            detalle.add(new EloEstimadoResponse.PosicionElo(pos,
                    eloCalculator.calcularCambio(eloPropio, pos, total, elosRivales)));
        }
        int deltaEsperado = detalle.stream()
                .filter(p -> p.posicion() == posicionEsperada)
                .map(EloEstimadoResponse.PosicionElo::deltaElo)
                .findFirst().orElse(0);
        return new EloEstimadoResponse(
                posicionEsperada,
                deltaEsperado,
                detalle.get(0).deltaElo(),
                detalle.get(detalle.size() - 1).deltaElo(),
                detalle);
    }

    /**
     * Con {@code ocultar} se le podan los datos sensibles de las carreras de un
     * campeonato privado a quien no es miembro: el calendario es publico, pero el
     * servidor, los links, el archivo y la cantidad de inscriptos no.
     */
    private CarreraResponse toResponse(Carrera carrera, Map<Long, Long> counts, boolean ocultar) {
        Long inscritos = ocultar ? null
                : (counts != null ? counts.getOrDefault(carrera.getId(), 0L) : null);
        return new CarreraResponse(
                carrera.getId(),
                carrera.getNombre(),
                carrera.getFecha(),
                carrera.getPracticaFecha(),
                carrera.getCircuito(),
                carrera.getCampeonato().getId(),
                carrera.getCampeonato().getNombre(),
                carrera.getCampeonato().getCategoria().getId(),
                carrera.getCampeonato().getCategoria().getNombre(),
                carrera.getEstado(),
                carrera.getCupoMaximo(),
                inscritos,
                ocultar ? null : carrera.getServidor(),
                ocultar || carrera.getArchivo() == null ? null : carrera.getArchivo().getId(),
                ocultar || carrera.getArchivo() == null ? null : carrera.getArchivo().getNombre(),
                ocultar ? null : carrera.getLinkPista(),
                ocultar ? null : carrera.getLinkAuto());
    }
}
