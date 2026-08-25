package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupCalificacionRequest;
import org.example.lfmnacional.dto.setup.SetupCalificacionResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.SetupCalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setups/{setupId}/calificaciones")
@RequiredArgsConstructor
public class SetupCalificacionController {

    private final SetupCalificacionService setupCalificacionService;

    @GetMapping
    public List<SetupCalificacionResponse> listarPorSetup(@PathVariable Long setupId) {
        return setupCalificacionService.listarPorSetup(setupId);
    }

    @PostMapping
    public ResponseEntity<SetupCalificacionResponse> calificar(
            @PathVariable Long setupId,
            @Valid @RequestBody SetupCalificacionRequest request,
            @AuthenticationPrincipal Usuario actual) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(setupCalificacionService.calificar(setupId, request, actual));
    }

    @DeleteMapping("/{calificacionId}")
    public ResponseEntity<Void> delete(@PathVariable Long calificacionId,
                                       @AuthenticationPrincipal Usuario actual) {
        setupCalificacionService.delete(calificacionId, actual);
        return ResponseEntity.noContent().build();
    }
}
