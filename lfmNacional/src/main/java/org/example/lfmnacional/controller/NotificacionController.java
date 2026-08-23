package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.notificacion.NotificacionRequest;
import org.example.lfmnacional.dto.notificacion.NotificacionResponse;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<NotificacionResponse> create(@Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.create(request));
    }

    @GetMapping("/{id}")
    public NotificacionResponse getById(@PathVariable Long id,
                                        @AuthenticationPrincipal Usuario actual) {
        Notificacion notificacion = notificacionService.getEntity(id);
        if (!notificacion.getUsuario().getId().equals(actual.getId())) {
            throw new BusinessException("No tenes permiso para ver esta notificacion");
        }
        return notificacionService.getById(id);
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
        Notificacion notificacion = notificacionService.getEntity(id);
        if (!notificacion.getUsuario().getId().equals(actual.getId())) {
            throw new BusinessException("No tenes permiso para marcar esta notificacion");
        }
        return notificacionService.marcarLeida(id);
    }

    @PutMapping("/me/leidas")
    public ResponseEntity<Void> marcarTodasLeidas(@AuthenticationPrincipal Usuario actual) {
        notificacionService.marcarTodasLeidas(actual.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Usuario actual) {
        Notificacion notificacion = notificacionService.getEntity(id);
        if (!notificacion.getUsuario().getId().equals(actual.getId())) {
            throw new BusinessException("No tenes permiso para borrar esta notificacion");
        }
        notificacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
