package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionRequest;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionResponse;
import org.example.lfmnacional.service.SesionClasificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clasificaciones")
@RequiredArgsConstructor
public class SesionClasificacionController {

    private final SesionClasificacionService sesionClasificacionService;

    @GetMapping
    public List<SesionClasificacionResponse> listAll() {
        return sesionClasificacionService.listAll();
    }

    @GetMapping("/{id}")
    public SesionClasificacionResponse getById(@PathVariable Long id) {
        return sesionClasificacionService.getById(id);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<SesionClasificacionResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return sesionClasificacionService.listarPorCarrera(carreraId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<SesionClasificacionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return sesionClasificacionService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<SesionClasificacionResponse> create(
            @Valid @RequestBody SesionClasificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionClasificacionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public SesionClasificacionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SesionClasificacionRequest request) {
        return sesionClasificacionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sesionClasificacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
