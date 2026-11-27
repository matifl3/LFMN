package org.example.lfmnacional.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.lfmnacional.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.service.SancionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PageResponse<SancionResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @AuthenticationPrincipal Usuario visor) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), PageResponse.MAX_SIZE),
                Sort.by("fecha").descending());
        Page<SancionResponse> result = sancionService.listAll(pageable, visor);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<SancionResponse> listarPorUsuario(@PathVariable Long usuarioId, @AuthenticationPrincipal Usuario visor) {
        return sancionService.listarPorUsuario(usuarioId, visor);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<SancionResponse> listarPorCarrera(@PathVariable Long carreraId, @AuthenticationPrincipal Usuario visor) {
        return sancionService.listarPorCarrera(carreraId, visor);
    }

    @GetMapping("/{id}")
    public SancionResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return sancionService.getById(id, visor);
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
