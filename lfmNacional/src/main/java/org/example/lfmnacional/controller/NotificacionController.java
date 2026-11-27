package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.notificacion.NotificacionRequest;
import org.example.lfmnacional.dto.notificacion.NotificacionResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificacionResponse> create(@Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.create(request));
    }

    @GetMapping("/{id}")
    public NotificacionResponse getById(@PathVariable Long id,
                                        @AuthenticationPrincipal Usuario actual) {
        return notificacionService.getById(id, actual);
    }

    @GetMapping("/me")
    public List<NotificacionResponse> listarPorUsuario(@AuthenticationPrincipal Usuario actual) {
        return notificacionService.listarPorUsuario(actual.getId());
    }

    @GetMapping("/me/no-leidas")
    public List<NotificacionResponse> listarNoLeidas(@AuthenticationPrincipal Usuario actual) {
        return notificacionService.listarNoLeidas(actual.getId());
    }

    @GetMapping("/me/no-leidas/contar")
    public long contarNoLeidas(@AuthenticationPrincipal Usuario actual) {
        return notificacionService.contarNoLeidas(actual.getId());
    }

    @PutMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id,
                                            @AuthenticationPrincipal Usuario actual) {
        return notificacionService.marcarLeida(id, actual);
    }

    @PutMapping("/me/leidas")
    public ResponseEntity<Void> marcarTodasLeidas(@AuthenticationPrincipal Usuario actual) {
        notificacionService.marcarTodasLeidas(actual.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Usuario actual) {
        notificacionService.delete(id, actual);
        return ResponseEntity.noContent().build();
    }
}
