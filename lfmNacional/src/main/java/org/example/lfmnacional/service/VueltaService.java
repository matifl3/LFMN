package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaAnalisisResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResumenResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VueltaService {

    private static final String TIPO_RACE = "RACE";

    private final VueltaRepository vueltaRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;

    @Transactional(readOnly = true)
    public List<VueltaResponse> listarPorCarrera(Long carreraId) {
        return vueltaRepository.findByCarrera_IdOrderByUsuario_IdAscNumeroVueltaAsc(carreraId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VueltaResponse> listarPorUsuarioEnCarrera(Long carreraId, Long usuarioId) {
        return vueltaRepository.findByCarrera_IdAndUsuario_IdOrderByNumeroVueltaAsc(carreraId, usuarioId)
                .stream().map(this::toResponse).toList();
    }

    private VueltaResponse toResponse(VueltaCarrera vuelta) {
        return new VueltaResponse(
                vuelta.getId(),
                vuelta.getCarrera().getId(),
                vuelta.getUsuario().getId(),
                vuelta.getUsuario().getNombrePiloto(),
                vuelta.getNumeroVuelta(),
                vuelta.getTiempoMs(),
                vuelta.getSector1(),
                vuelta.getSector2(),
                vuelta.getSector3(),
                vuelta.getCortes(),
                vuelta.getNeumatico(),
                vuelta.getTipo());
    }

    @Transactional(readOnly = true)
    public List<VueltaAnalisisResponse> analisisCarrera(Long carreraId, Long usuarioId) {
        List<VueltaCarrera> todas = vueltaRepository.findByCarrera_IdOrderByUsuario_IdAscNumeroVueltaAsc(carreraId);
        List<VueltaCarrera> raceLaps = todas.stream()
                .filter(v -> TIPO_RACE.equals(v.getTipo()))
                .toList();
        Map<Integer, List<VueltaCarrera>> porVuelta = raceLaps.stream()
                .collect(Collectors.groupingBy(VueltaCarrera::getNumeroVuelta));
        Map<Long, Map<Integer, Long>> cumulative = new HashMap<>();
        for (VueltaCarrera v : raceLaps) {
            Long uid = v.getUsuario().getId();
            int lap = v.getNumeroVuelta();
            long cumPrev = cumulative.getOrDefault(uid, Map.of()).getOrDefault(lap - 1, 0L);
            cumulative.computeIfAbsent(uid, k -> new HashMap<>()).put(lap, cumPrev + v.getTiempoMs());
        }
        List<VueltaCarrera> usuarioVueltas = raceLaps.stream()
                .filter(v -> v.getUsuario().getId().equals(usuarioId))
                .sorted(Comparator.comparing(VueltaCarrera::getNumeroVuelta))
                .toList();
        List<VueltaAnalisisResponse> resultado = new ArrayList<>();
        for (VueltaCarrera v : usuarioVueltas) {
            int lap = v.getNumeroVuelta();
            long cumMio = cumulative.getOrDefault(usuarioId, Map.of()).getOrDefault(lap, 0L);
            long minCum = porVuelta.getOrDefault(lap, List.of()).stream()
                    .mapToLong(v2 -> cumulative.getOrDefault(v2.getUsuario().getId(), Map.of())
                            .getOrDefault(lap, Long.MAX_VALUE))
                    .min().orElse(cumMio);
            long myCum = cumMio;
            int position = 1 + (int) porVuelta.getOrDefault(lap, List.of()).stream()
                    .filter(v2 -> !v2.getUsuario().getId().equals(usuarioId))
                    .mapToLong(v2 -> cumulative.getOrDefault(v2.getUsuario().getId(), Map.of())
                            .getOrDefault(lap, Long.MAX_VALUE))
                    .filter(c -> c < myCum)
                    .count();
            resultado.add(new VueltaAnalisisResponse(
                    v.getId(), v.getNumeroVuelta(), v.getTiempoMs(),
                    v.getSector1(), v.getSector2(), v.getSector3(),
                    v.getCortes(), v.getNeumatico(),
                    cumMio == minCum ? 0L : cumMio - minCum,
                    position));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public VueltaResumenResponse resumenCarrera(Long carreraId, Long usuarioId) {
        List<VueltaCarrera> todas = vueltaRepository.findByCarrera_IdOrderByUsuario_IdAscNumeroVueltaAsc(carreraId);
        List<VueltaCarrera> raceLaps = todas.stream()
                .filter(v -> TIPO_RACE.equals(v.getTipo()))
                .toList();
        List<VueltaCarrera> usuarioVueltas = raceLaps.stream()
                .filter(v -> v.getUsuario().getId().equals(usuarioId))
                .sorted(Comparator.comparing(VueltaCarrera::getNumeroVuelta))
                .toList();

        List<VueltaCarrera> validas = usuarioVueltas.stream()
                .filter(v -> v.getTiempoMs() != null && v.getTiempoMs() > 0)
                .toList();

        Long mejorVueltaMs = validas.stream().min(Comparator.comparingLong(VueltaCarrera::getTiempoMs))
                .map(VueltaCarrera::getTiempoMs).orElse(null);
        Integer numeroVueltaMejor = validas.stream().min(Comparator.comparingLong(VueltaCarrera::getTiempoMs))
                .map(VueltaCarrera::getNumeroVuelta).orElse(null);

        Long mejorS1 = minSector(usuarioVueltas, 1);
        Long mejorS2 = minSector(usuarioVueltas, 2);
        Long mejorS3 = minSector(usuarioVueltas, 3);
        Long teoricaMs = (mejorS1 != null && mejorS2 != null && mejorS3 != null)
                ? mejorS1 + mejorS2 + mejorS3 : null;
        Long potencialMs = (mejorVueltaMs != null && teoricaMs != null)
                ? Math.max(0L, mejorVueltaMs - teoricaMs) : null;

        Long mejorS1Parrilla = minSector(raceLaps, 1);
        Long mejorS2Parrilla = minSector(raceLaps, 2);
        Long mejorS3Parrilla = minSector(raceLaps, 3);
        Long teoricaParrillaMs = (mejorS1Parrilla != null && mejorS2Parrilla != null && mejorS3Parrilla != null)
                ? mejorS1Parrilla + mejorS2Parrilla + mejorS3Parrilla : null;

        int vueltasTotales = validas.size();
        Long mediaMs = vueltasTotales > 0
                ? validas.stream().mapToLong(VueltaCarrera::getTiempoMs).sum() / vueltasTotales
                : null;
        Long desvioMs = null;
        if (mediaMs != null) {
            double sumSq = validas.stream()
                    .mapToDouble(v -> {
                        double d = v.getTiempoMs() - mediaMs;
                        return d * d;
                    }).sum();
            desvioMs = Math.round(Math.sqrt(sumSq / vueltasTotales));
        }

        int dentroDe500ms = 0;
        int dentroDe1s = 0;
        if (mejorVueltaMs != null) {
            for (VueltaCarrera v : validas) {
                long diff = v.getTiempoMs() - mejorVueltaMs;
                if (diff <= 500) dentroDe500ms++;
                if (diff <= 1000) dentroDe1s++;
            }
        }

        Integer posicionGrilla = null;
        List<SesionClasificacion> clasificacion =
                sesionClasificacionRepository.findByCarrera_IdOrderByTiempoAsc(carreraId);
        for (int i = 0; i < clasificacion.size(); i++) {
            if (clasificacion.get(i).getUsuario().getId().equals(usuarioId)) {
                posicionGrilla = i + 1;
                break;
            }
        }

        Integer posicionFinal = null;
        ResultadoCarrera resultado =
                resultadoCarreraRepository.findByCarrera_IdAndUsuario_Id(carreraId, usuarioId).orElse(null);
        if (resultado != null) {
            posicionFinal = resultado.getPosicionFinal();
        }
        Integer posicionesGanadas = (posicionGrilla != null && posicionFinal != null)
                ? posicionGrilla - posicionFinal : null;

        Integer posicionPico = analisisCarrera(carreraId, usuarioId).stream()
                .map(VueltaAnalisisResponse::posicionEnVuelta)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);

        return new VueltaResumenResponse(
                mejorVueltaMs, numeroVueltaMejor,
                mejorS1, mejorS2, mejorS3,
                teoricaMs, potencialMs,
                mejorS1Parrilla, mejorS2Parrilla, mejorS3Parrilla, teoricaParrillaMs,
                vueltasTotales, mediaMs, desvioMs,
                dentroDe500ms, dentroDe1s,
                posicionGrilla, posicionFinal, posicionesGanadas, posicionPico);
    }

    private Long minSector(List<VueltaCarrera> vueltas, int sector) {
        return vueltas.stream()
                .map(v -> sector == 1 ? v.getSector1() : sector == 2 ? v.getSector2() : v.getSector3())
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(null);
    }
}
