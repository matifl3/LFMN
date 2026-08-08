package org.example.lfmnacional.controller;

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
    public List<VotoResponse> listarPorIncidente(@PathVariable Long incidenteId) {
        return votoComisarioService.listarPorIncidente(incidenteId);
    }

    @GetMapping("/comisario/{comisarioId}")
    public List<VotoResponse> listarPorComisario(@PathVariable Long comisarioId) {
        return votoComisarioService.listarPorComisario(comisarioId);
    }

    @GetMapping("/{id}")
    public VotoResponse getById(@PathVariable Long id) {
        return votoComisarioService.getById(id);
    }
}
