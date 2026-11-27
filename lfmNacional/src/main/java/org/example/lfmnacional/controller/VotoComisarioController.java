package org.example.lfmnacional.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.lfmnacional.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.VotoResponse;
import org.example.lfmnacional.service.VotoComisarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votos")
@RequiredArgsConstructor
public class VotoComisarioController {

    private final VotoComisarioService votoComisarioService;

    @GetMapping("/incidente/{incidenteId}")
    public List<VotoResponse> listarPorIncidente(@PathVariable Long incidenteId, @AuthenticationPrincipal Usuario visor) {
        return votoComisarioService.listarPorIncidente(incidenteId, visor);
    }

    @GetMapping("/comisario/{comisarioId}")
    public List<VotoResponse> listarPorComisario(@PathVariable Long comisarioId, @AuthenticationPrincipal Usuario visor) {
        return votoComisarioService.listarPorComisario(comisarioId, visor);
    }

    @GetMapping("/{id}")
    public VotoResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return votoComisarioService.getById(id, visor);
    }
}
