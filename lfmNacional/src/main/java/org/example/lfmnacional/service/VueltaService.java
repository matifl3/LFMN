package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaAnalisisResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.repository.VueltaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VueltaService {

    private final VueltaRepository vueltaRepository;

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
                .filter(v -> "RACE".equals(v.getTipo()))
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
}
