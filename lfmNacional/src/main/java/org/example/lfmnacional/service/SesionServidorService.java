package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraRequest;
import org.example.lfmnacional.dto.sesion.CarSesionData;
import org.example.lfmnacional.dto.sesion.EventoSesionData;
import org.example.lfmnacional.dto.sesion.LapSesionData;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesionServidorService {

    private static final long SIN_VUELTA_VALIDA = 999999999L;
    private static final long SIN_TIEMPO = 0L;
    private static final String SESION_QUALIFY = "QUALIFY";
    private static final String SESION_RACE = "RACE";

    private final CarreraService carreraService;
    private final CarreraRepository carreraRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResultadoCarreraService resultadoCarreraService;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final IncidenteRepository incidenteRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final SesionProcesadaRepository sesionProcesadaRepository;
    private final VueltaRepository vueltaRepository;

    @Transactional
    public String importarSesion(Long carreraId, SesionServerData sesion) {
        Carrera carrera = carreraService.getEntity(carreraId);
        return importar(carrera, sesion);
    }

    @Transactional(readOnly = true)
    public Carrera resolverCarrera(SesionServerData sesion, LocalDateTime momentoSesion) {
        if (sesion == null || sesion.trackName() == null || sesion.trackName().isBlank()) {
            throw new BusinessException("El JSON de sesion no permite asociar una carrera (falta trackName)");
        }
        String track = normalizar(sesion.trackName());
        Carrera mejor = null;
        long menorDiferencia = Long.MAX_VALUE;
        for (Carrera carrera : carreraRepository.findAll()) {
            if (carrera.getCircuito() == null || carrera.getCircuito().isBlank()) {
                continue;
            }
            String circuito = normalizar(carrera.getCircuito());
            if (circuito.contains(track) || track.contains(circuito)) {
                long diferencia = Math.abs(Duration.between(carrera.getFecha(), momentoSesion).toMinutes());
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejor = carrera;
                }
            }
        }
        if (mejor == null) {
            throw new BusinessException(
                    "No se encontro una carrera para el circuito '" + sesion.trackName() + "'");
        }
        return mejor;
    }

    private String importar(Carrera carrera, SesionServerData sesion) {
        if (sesion == null) {
            throw new BusinessException("El JSON de sesion es invalido");
        }
        String tipo = sesion.type() != null ? sesion.type().toUpperCase() : "";
        switch (tipo) {
            case SESION_QUALIFY -> importarClasificacion(carrera, sesion);
            case SESION_RACE -> {
                importarResultados(carrera, sesion);
                importarVueltas(carrera, sesion, tipo);
            }
            case "PRACTICE" -> log.info("Sesion PRACTICE ignorada (no importa datos)");
            default -> throw new BusinessException("Tipo de sesion no soportado: " + sesion.type());
        }
        autogenerarIncidentes(carrera, sesion);
        return tipo;
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("_", " ")
                .trim();
    }

    @Transactional
    public void registrarProcesada(Long carreraId, String nombreArchivo, String tipo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        if (sesionProcesadaRepository.existsByNombreArchivo(nombreArchivo)) {
            return;
        }
        sesionProcesadaRepository.save(SesionProcesada.builder()
                .carrera(carrera)
                .nombreArchivo(nombreArchivo)
                .tipo(tipo)
                .build());
    }

    public boolean yaProcesada(String nombreArchivo) {
        return sesionProcesadaRepository.existsByNombreArchivo(nombreArchivo);
    }

    private void importarClasificacion(Carrera carrera, SesionServerData sesion) {
        if (sesion.result() == null) {
            return;
        }
        List<ResultadoSesionData> conTiempo = sesion.result().stream()
                .filter(r -> guidValido(r.driverGuid()))
                .filter(r -> r.bestLap() != null && r.bestLap() > 0 && r.bestLap() < SIN_VUELTA_VALIDA)
                .sorted(Comparator.comparing(ResultadoSesionData::bestLap))
                .toList();
        if (conTiempo.isEmpty()) {
            return;
        }

        Long tiempoPole = conTiempo.get(0).bestLap();
        Map<Integer, CarSesionData> autos = autosPorId(sesion);
        for (ResultadoSesionData dato : conTiempo) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            SesionClasificacion clasificacion = sesionClasificacionRepository
                    .findByCarrera_IdAndUsuario_Id(carrera.getId(), usuario.getId())
                    .orElseGet(() -> SesionClasificacion.builder()
                            .carrera(carrera)
                            .usuario(usuario)
                            .build());
            clasificacion.setFecha(LocalDateTime.now());
            clasificacion.setTiempo(dato.bestLap());
            clasificacion.setDiferenciaPole(dato.bestLap() - tiempoPole);
            clasificacion.setModeloAuto(modeloDe(autos, dato));
            clasificacion.setSkinAuto(skinDe(autos, dato));
            sesionClasificacionRepository.save(clasificacion);
        }

        ResultadoSesionData pole = conTiempo.get(0);
        usuarioRepository.findByGuidSteam(pole.driverGuid()).ifPresent(usuario ->
                resultadoCarreraRepository
                        .findByCarrera_IdAndUsuario_Id(carrera.getId(), usuario.getId())
                        .ifPresent(resultado -> {
                            resultado.setPoles(true);
                            resultadoCarreraRepository.save(resultado);
                        }));
    }

    private void importarResultados(Carrera carrera, SesionServerData sesion) {
        if (sesion.result() == null) {
            return;
        }
        List<ResultadoSesionData> conGuid = sesion.result().stream()
                .filter(r -> guidValido(r.driverGuid()))
                .toList();
        if (conGuid.isEmpty()) {
            return;
        }

        List<ResultadoSesionData> finalizaron = conGuid.stream()
                .filter(r -> r.totalTime() != null && r.totalTime() > SIN_TIEMPO)
                .sorted(Comparator.comparing(ResultadoSesionData::totalTime))
                .toList();
        List<ResultadoSesionData> dnf = conGuid.stream()
                .filter(r -> r.totalTime() == null || r.totalTime() <= SIN_TIEMPO)
                .sorted(Comparator.comparing(
                        (ResultadoSesionData r) -> vueltasCompletadas(sesion, r.driverGuid()),
                        Comparator.reverseOrder()))
                .toList();

        List<ResultadoCarreraRequest> items = new ArrayList<>();
        Map<Integer, CarSesionData> autos = autosPorId(sesion);
        int posicion = 1;
        for (ResultadoSesionData dato : finalizaron) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            items.add(new ResultadoCarreraRequest(
                    carrera.getId(), usuario.getId(), posicion++,
                    dato.totalTime(), vueltaRapidaValida(dato.bestLap()),
                    null, true, null, null,
                    modeloDe(autos, dato), skinDe(autos, dato)));
        }
        for (ResultadoSesionData dato : dnf) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            items.add(new ResultadoCarreraRequest(
                    carrera.getId(), usuario.getId(), posicion++,
                    dato.totalTime(), vueltaRapidaValida(dato.bestLap()),
                    null, false, null, null,
                    modeloDe(autos, dato), skinDe(autos, dato)));
        }

        if (items.isEmpty()) {
            return;
        }
        resultadoCarreraService.cargarResultados(new CargarResultadosRequest(carrera.getId(), items));
    }

    private Map<Integer, CarSesionData> autosPorId(SesionServerData sesion) {
        Map<Integer, CarSesionData> mapa = new HashMap<>();
        if (sesion.cars() != null) {
            for (CarSesionData auto : sesion.cars()) {
                if (auto.carId() != null) {
                    mapa.put(auto.carId(), auto);
                }
            }
        }
        return mapa;
    }

    private String modeloDe(Map<Integer, CarSesionData> autos, ResultadoSesionData dato) {
        if (dato.carModel() != null && !dato.carModel().isBlank()) {
            return dato.carModel();
        }
        CarSesionData auto = dato.carId() != null ? autos.get(dato.carId()) : null;
        return auto != null ? auto.model() : null;
    }

    private String skinDe(Map<Integer, CarSesionData> autos, ResultadoSesionData dato) {
        CarSesionData auto = dato.carId() != null ? autos.get(dato.carId()) : null;
        return auto != null ? auto.skin() : null;
    }

    private void importarVueltas(Carrera carrera, SesionServerData sesion, String tipo) {
        if (sesion.laps() == null || sesion.laps().isEmpty()) {
            return;
        }
        vueltaRepository.deleteByCarrera_IdAndTipo(carrera.getId(), tipo);
        Map<String, Integer> numeros = new HashMap<>();
        List<VueltaCarrera> vueltas = new ArrayList<>();
        for (LapSesionData lap : sesion.laps()) {
            if (!guidValido(lap.driverGuid())) {
                continue;
            }
            Usuario usuario = usuarioRepository.findByGuidSteam(lap.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            int numero = numeros.merge(lap.driverGuid(), 1, Integer::sum);
            vueltas.add(VueltaCarrera.builder()
                    .carrera(carrera)
                    .usuario(usuario)
                    .numeroVuelta(numero)
                    .tiempoMs(lap.lapTime())
                    .sector1(sectorDe(lap, 0))
                    .sector2(sectorDe(lap, 1))
                    .sector3(sectorDe(lap, 2))
                    .cortes(lap.cuts())
                    .neumatico(lap.tyre())
                    .tipo(tipo)
                    .build());
        }
        vueltaRepository.saveAll(vueltas);
    }

    private Long sectorDe(LapSesionData lap, int indice) {
        if (lap.sectors() == null || lap.sectors().size() <= indice) {
            return null;
        }
        Integer sector = lap.sectors().get(indice);
        return sector != null ? sector.longValue() : null;
    }

    private void autogenerarIncidentes(Carrera carrera, SesionServerData sesion) {
        if (sesion.events() == null) {
            return;
        }
        for (EventoSesionData evento : sesion.events()) {
            String guid = evento.driver() != null ? evento.driver().guid() : null;
            if (!guidValido(guid)) {
                continue;
            }
            Usuario piloto = usuarioRepository.findByGuidSteam(guid).orElse(null);
            if (piloto == null) {
                continue;
            }
            String descripcion = "Colision detectada automaticamente (" + evento.type() + ")"
                    + (evento.impactSpeed() != null ? " - Impacto: " + Math.round(evento.impactSpeed()) + " km/h" : "");
            Incidente incidente = incidenteRepository.save(Incidente.builder()
                    .carrera(carrera)
                    .reportante(piloto)
                    .descripcion(descripcion)
                    .estado(EstadoIncidente.PENDIENTE)
                    .build());
            incidentePilotoRepository.save(IncidentePiloto.builder()
                    .incidente(incidente)
                    .usuario(piloto)
                    .rol(RolPilotoIncidente.AFECTADO)
                    .build());
        }
    }

    private boolean guidValido(String guid) {
        return guid != null && !guid.isBlank();
    }

    private Long vueltaRapidaValida(Long bestLap) {
        if (bestLap == null || bestLap <= 0 || bestLap >= SIN_VUELTA_VALIDA) {
            return null;
        }
        return bestLap;
    }

    private long vueltasCompletadas(SesionServerData sesion, String guid) {
        if (sesion.laps() == null) {
            return 0;
        }
        return sesion.laps().stream()
                .filter(l -> guid.equals(l.driverGuid()))
                .count();
    }
}
