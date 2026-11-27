package org.example.lfmnacional.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.lfmnacional.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.ResolucionResponse;
import org.example.lfmnacional.dto.incidente.ResolucionUpdateRequest;
import org.example.lfmnacional.service.ResolucionIncidenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resoluciones")
@RequiredArgsConstructor
public class ResolucionIncidenteController {

    private final ResolucionIncidenteService resolucionIncidenteService;

    @GetMapping
    public List<ResolucionResponse> listAll(@AuthenticationPrincipal Usuario visor) {
        return resolucionIncidenteService.listAll(visor);
    }

    @GetMapping("/{id}")
    public ResolucionResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return resolucionIncidenteService.getById(id, visor);
    }

    @GetMapping("/comisario/{comisarioId}")
    public List<ResolucionResponse> listarPorComisario(@PathVariable Long comisarioId, @AuthenticationPrincipal Usuario visor) {
        return resolucionIncidenteService.listarPorComisario(comisarioId, visor);
    }

    @PutMapping("/{id}/explicacion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResolucionResponse updateExplicacion(
            @PathVariable Long id,
            @Valid @RequestBody ResolucionUpdateRequest request) {
        return resolucionIncidenteService.updateExplicacion(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resolucionIncidenteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
