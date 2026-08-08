package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.notificacion.NotificacionRequest;
import org.example.lfmnacional.dto.notificacion.NotificacionResponse;
import org.example.lfmnacional.service.NotificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public NotificacionResponse getById(@PathVariable Long id) {
        return notificacionService.getById(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<NotificacionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return notificacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public List<NotificacionResponse> listarNoLeidas(@PathVariable Long usuarioId) {
        return notificacionService.listarNoLeidas(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas/contar")
    public long contarNoLeidas(@PathVariable Long usuarioId) {
        return notificacionService.contarNoLeidas(usuarioId);
    }

    @PutMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id) {
        return notificacionService.marcarLeida(id);
    }

    @PutMapping("/usuario/{usuarioId}/leidas")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable Long usuarioId) {
        notificacionService.marcarTodasLeidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
