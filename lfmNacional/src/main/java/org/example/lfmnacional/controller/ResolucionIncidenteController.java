package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.ResolucionResponse;
import org.example.lfmnacional.dto.incidente.ResolucionUpdateRequest;
import org.example.lfmnacional.service.ResolucionIncidenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resoluciones")
@RequiredArgsConstructor
public class ResolucionIncidenteController {

    private final ResolucionIncidenteService resolucionIncidenteService;

    @GetMapping
    public List<ResolucionResponse> listAll() {
        return resolucionIncidenteService.listAll();
    }

    @GetMapping("/{id}")
    public ResolucionResponse getById(@PathVariable Long id) {
        return resolucionIncidenteService.getById(id);
    }

    @GetMapping("/comisario/{comisarioId}")
    public List<ResolucionResponse> listarPorComisario(@PathVariable Long comisarioId) {
        return resolucionIncidenteService.listarPorComisario(comisarioId);
    }

    @PutMapping("/{id}/explicacion")
    public ResolucionResponse updateExplicacion(
            @PathVariable Long id,
            @Valid @RequestBody ResolucionUpdateRequest request) {
        return resolucionIncidenteService.updateExplicacion(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resolucionIncidenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
