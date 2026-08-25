package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarreraResolverService {

    private static final long VENTANA_HORAS = 3;

    private final CarreraRepository carreraRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    @Transactional(readOnly = true)
    public Carrera resolverCarrera(SesionServerData sesion, LocalDateTime momentoSesion) {
        if (sesion == null || sesion.result() == null) {
            throw new BusinessException("El JSON de sesion no contiene datos para resolver carrera");
        }

        Set<String> guids = sesion.result().stream()
                .map(ResultadoSesionData::driverGuid)
                .filter(this::guidValido)
                .collect(Collectors.toSet());

        if (!guids.isEmpty()) {
            Carrera porInscripciones = resolverPorInscripciones(guids);
            if (porInscripciones != null) {
                log.info("Carrera resuelta por inscripciones: {} (pilotos matching: {})",
                        porInscripciones.getNombre(), guids.size());
                return porInscripciones;
            }
        }

        if (momentoSesion != null) {
            Carrera porFecha = resolverPorFecha(momentoSesion);
            if (porFecha != null) {
                log.info("Carrera resuelta por fecha proxima: {} (sesion: {})",
                        porFecha.getNombre(), momentoSesion);
                return porFecha;
            }
        }

        String pistas = guids.isEmpty() ? "(sin pilotos validos)"
                : guids.stream().map(this::normalizar).collect(Collectors.joining(", "));
        throw new BusinessException(
                "No se encontro carrera para la sesion del " + momentoSesion
                        + ". Track: '" + sesion.trackName()
                        + "'. Pilotos en JSON: [" + pistas + "]");
    }

    private Carrera resolverPorInscripciones(Set<String> guids) {
        Map<Long, Long> conteoPorCarrera = new HashMap<>();
        Map<Long, Carrera> carreraCache = new HashMap<>();

        for (String guid : guids) {
            usuarioRepository.findByGuidSteam(guid).ifPresent(usuario ->
                    inscripcionRepository.findByUsuario_Id(usuario.getId()).stream()
                            .filter(i -> i.getEstado() == EstadoInscripcion.INSCRIPTO)
                            .forEach(i -> {
                                Long carreraId = i.getCarrera().getId();
                                conteoPorCarrera.merge(carreraId, 1L, Long::sum);
                                carreraCache.put(carreraId, i.getCarrera());
                            }));
        }

        if (conteoPorCarrera.isEmpty()) {
            return null;
        }

        long maxCount = Collections.max(conteoPorCarrera.values());
        List<Long> candidatos = conteoPorCarrera.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();

        return carreraCache.get(candidatos.get(0));
    }

    private Carrera resolverPorFecha(LocalDateTime momentoSesion) {
        LocalDateTime desde = momentoSesion.minusHours(VENTANA_HORAS);
        LocalDateTime hasta = momentoSesion.plusHours(VENTANA_HORAS);

        Carrera mejor = null;
        long menorDiferencia = Long.MAX_VALUE;

        for (Carrera carrera : carreraRepository.findAll()) {
            if (carrera.getFecha() == null) {
                continue;
            }
            if (carrera.getFecha().isBefore(desde) || carrera.getFecha().isAfter(hasta)) {
                continue;
            }
            long diferencia = Math.abs(Duration.between(carrera.getFecha(), momentoSesion).toMinutes());
            if (diferencia < menorDiferencia) {
                menorDiferencia = diferencia;
                mejor = carrera;
            }
        }
        return mejor;
    }

    boolean guidValido(String guid) {
        return guid != null && !guid.isBlank();
    }

    private String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("_", " ")
                .trim();
    }
}
