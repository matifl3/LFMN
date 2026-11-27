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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.example.lfmnacional.entity.Usuario;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apelaciones")
@RequiredArgsConstructor
public class ApelacionController {

    private final ApelacionService apelacionService;

    @PostMapping
    public ResponseEntity<ApelacionResponse> create(@Valid @RequestBody ApelacionRequest request,
                                                     @AuthenticationPrincipal Usuario actual) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apelacionService.create(request, actual));
    }

    @GetMapping
    public List<ApelacionResponse> listAll(@AuthenticationPrincipal Usuario visor) {
        return apelacionService.listAll(visor);
    }

    @GetMapping("/pendientes")
    public List<ApelacionResponse> listarPendientes(@AuthenticationPrincipal Usuario visor) {
        return apelacionService.listarPendientes(visor);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ApelacionResponse> listarPorUsuario(@PathVariable Long usuarioId, @AuthenticationPrincipal Usuario visor) {
        return apelacionService.listarPorUsuario(usuarioId, visor);
    }

    @GetMapping("/{id}")
    public ApelacionResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario visor) {
        return apelacionService.getById(id, visor);
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
