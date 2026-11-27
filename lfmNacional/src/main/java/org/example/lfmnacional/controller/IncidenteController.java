package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.incidente.*;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.service.IncidenteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    @PostMapping
    public ResponseEntity<IncidenteResponse> reportar(@AuthenticationPrincipal Usuario actual,
                                                      @Valid @RequestBody IncidenteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.reportar(request, actual));
    }

    @GetMapping
    public PageResponse<IncidenteResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @AuthenticationPrincipal Usuario visor) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), PageResponse.MAX_SIZE),
                Sort.by("fecha").descending());
        Page<IncidenteResponse> result = incidenteService.listAll(pageable, visor);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/estado/{estado}")
    public List<IncidenteResponse> listarPorEstado(@PathVariable EstadoIncidente estado, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.listarPorEstado(estado, visor);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<IncidenteResponse> listarPorCarrera(@PathVariable Long carreraId, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.listarPorCarrera(carreraId, visor);
    }

    @GetMapping("/reportante/{reportanteId}")
    public List<IncidenteResponse> listarPorReportante(@PathVariable Long reportanteId, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.listarPorReportante(reportanteId, visor);
    }

    @GetMapping("/comisario/{comisarioId}/decisiones")
    public List<DecisionComisarioResponse> listarDecisionesComisario(@PathVariable Long comisarioId) {
        return incidenteService.listarDecisionesComisario(comisarioId);
    }

    @GetMapping("/{id}")
    public IncidenteResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.getById(id, visor);
    }

    @GetMapping("/{id}/pilotos")
    public List<IncidentePilotoResponse> listarPilotos(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.listarPilotos(id, visor);
    }

    @GetMapping("/{id}/resolucion")
    public ResolucionResponse getResolucion(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return incidenteService.getResolucion(id, visor);
    }

    @PostMapping("/{id}/pilotos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public List<IncidentePilotoResponse> asignarPilotos(
            @PathVariable Long id,
            @Valid @RequestBody List<IncidentePilotoRequest> pilotos) {
        return incidenteService.asignarPilotos(id, pilotos);
    }

    @PostMapping("/{id}/votos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public VotoResponse votar(@AuthenticationPrincipal Usuario actual,
                              @PathVariable Long id, @Valid @RequestBody VotoRequest request) {
        return incidenteService.votar(id, request, actual);
    }

    @PostMapping("/{id}/resolucion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<ResolucionResponse> guardarResolucion(
            @AuthenticationPrincipal Usuario actual,
            @PathVariable Long id,
            @Valid @RequestBody ResolucionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.guardarResolucion(id, request, actual));
    }
}
