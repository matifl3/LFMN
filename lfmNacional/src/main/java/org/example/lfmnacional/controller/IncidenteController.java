package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.incidente.*;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.service.IncidenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<IncidenteResponse> reportar(@Valid @RequestBody IncidenteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.reportar(request));
    }

    @GetMapping
    public List<IncidenteResponse> listAll() {
        return incidenteService.listAll();
    }

    @GetMapping("/estado/{estado}")
    public List<IncidenteResponse> listarPorEstado(@PathVariable EstadoIncidente estado) {
        return incidenteService.listarPorEstado(estado);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<IncidenteResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return incidenteService.listarPorCarrera(carreraId);
    }

    @GetMapping("/reportante/{reportanteId}")
    public List<IncidenteResponse> listarPorReportante(@PathVariable Long reportanteId) {
        return incidenteService.listarPorReportante(reportanteId);
    }

    @GetMapping("/comisario/{comisarioId}/decisiones")
    public List<DecisionComisarioResponse> listarDecisionesComisario(@PathVariable Long comisarioId) {
        return incidenteService.listarDecisionesComisario(comisarioId);
    }

    @GetMapping("/{id}")
    public IncidenteResponse getById(@PathVariable Long id) {
        return incidenteService.getById(id);
    }

    @GetMapping("/{id}/pilotos")
    public List<IncidentePilotoResponse> listarPilotos(@PathVariable Long id) {
        return incidenteService.listarPilotos(id);
    }

    @GetMapping("/{id}/resolucion")
    public ResolucionResponse getResolucion(@PathVariable Long id) {
        return incidenteService.getResolucion(id);
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
    public VotoResponse votar(@PathVariable Long id, @Valid @RequestBody VotoRequest request) {
        return incidenteService.votar(id, request);
    }

    @PostMapping("/{id}/resolucion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<ResolucionResponse> guardarResolucion(
            @PathVariable Long id,
            @Valid @RequestBody ResolucionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenteService.guardarResolucion(id, request));
    }
}
