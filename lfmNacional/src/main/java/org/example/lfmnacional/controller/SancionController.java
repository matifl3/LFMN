package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.service.SancionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
@RequiredArgsConstructor
public class SancionController {

    private final SancionService sancionService;

    @GetMapping
    public List<SancionResponse> listAll() {
        return sancionService.listAll();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<SancionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return sancionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<SancionResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return sancionService.listarPorCarrera(carreraId);
    }

    @GetMapping("/{id}")
    public SancionResponse getById(@PathVariable Long id) {
        return sancionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<SancionResponse> create(@Valid @RequestBody SancionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sancionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public SancionResponse update(@PathVariable Long id, @Valid @RequestBody SancionRequest request) {
        return sancionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sancionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
