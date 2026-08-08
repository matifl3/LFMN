package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.apelacion.ApelacionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResolucionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResponse;
import org.example.lfmnacional.service.ApelacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apelaciones")
@RequiredArgsConstructor
public class ApelacionController {

    private final ApelacionService apelacionService;

    @PostMapping
    public ResponseEntity<ApelacionResponse> create(@Valid @RequestBody ApelacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apelacionService.create(request));
    }

    @GetMapping
    public List<ApelacionResponse> listAll() {
        return apelacionService.listAll();
    }

    @GetMapping("/pendientes")
    public List<ApelacionResponse> listarPendientes() {
        return apelacionService.listarPendientes();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ApelacionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return apelacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/{id}")
    public ApelacionResponse getById(@PathVariable Long id) {
        return apelacionService.getById(id);
    }

    @PutMapping("/{id}/resolucion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ApelacionResponse resolver(@PathVariable Long id, @Valid @RequestBody ApelacionResolucionRequest request) {
        return apelacionService.resolver(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        apelacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
