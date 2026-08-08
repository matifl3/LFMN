package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.VotoResponse;
import org.example.lfmnacional.entity.VotoComisario;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VotoComisarioService {

    private final VotoComisarioRepository votoComisarioRepository;

    @Transactional(readOnly = true)
    public List<VotoResponse> listarPorIncidente(Long incidenteId) {
        return votoComisarioRepository.findByIncidente_Id(incidenteId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VotoResponse> listarPorComisario(Long comisarioId) {
        return votoComisarioRepository.findByComisario_IdOrderByFechaDesc(comisarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VotoResponse getById(Long id) {
        VotoComisario voto = votoComisarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto no encontrado con id " + id));
        return toResponse(voto);
    }

    private VotoResponse toResponse(VotoComisario voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getIncidente().getId(),
                voto.getComisario().getId(),
                voto.getDecision(),
                voto.getComentario(),
                voto.getFecha());
    }
}
