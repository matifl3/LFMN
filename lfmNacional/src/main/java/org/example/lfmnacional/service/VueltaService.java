package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.repository.VueltaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
