package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Usuario;
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
    private final CampeonatoAccesoService accesoService;

    @Transactional(readOnly = true)
    public List<VotoResponse> listarPorIncidente(Long incidenteId, Usuario visor) {
        return visibles(votoComisarioRepository.findByIncidente_Id(incidenteId), visor);
    }

    @Transactional(readOnly = true)
    public List<VotoResponse> listarPorComisario(Long comisarioId, Usuario visor) {
        return visibles(votoComisarioRepository.findByComisario_IdOrderByFechaDesc(comisarioId), visor);
    }

    @Transactional(readOnly = true)
    public VotoResponse getById(Long id, Usuario visor) {
        VotoComisario voto = votoComisarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto no encontrado con id " + id));
        accesoService.exigirVeCarreraSiHay(visor, voto.getIncidente().getCarrera());
        return toResponse(voto);
    }

    private List<VotoResponse> visibles(List<VotoComisario> votos, Usuario visor) {
        var ve = accesoService.filtroVeCarrera(visor);
        return votos.stream()
                .filter(v -> ve.test(v.getIncidente().getCarrera()))
                .map(this::toResponse).toList();
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
