package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.LogroRequest;
import org.example.lfmnacional.dto.logro.LogroResponse;
import org.example.lfmnacional.dto.logro.RecompensaRequest;
import org.example.lfmnacional.dto.logro.RecompensaResponse;
import org.example.lfmnacional.dto.logro.UsuarioLogroResponse;
import org.example.lfmnacional.dto.logro.UsuarioRecompensaResponse;
import org.example.lfmnacional.service.LogroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logros")
@RequiredArgsConstructor
public class LogroController {

    private final LogroService logroService;

    @GetMapping
    public List<LogroResponse> listAll() {
        return logroService.listAll();
    }

    @GetMapping("/{id}")
    public LogroResponse getById(@PathVariable Long id) {
        return logroService.getById(id);
    }

    @PostMapping
    public ResponseEntity<LogroResponse> create(@Valid @RequestBody LogroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logroService.create(request));
    }

    @PutMapping("/{id}")
    public LogroResponse update(@PathVariable Long id, @Valid @RequestBody LogroRequest request) {
        return logroService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logroService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{logroId}/recompensas")
    public ResponseEntity<RecompensaResponse> agregarRecompensa(
            @PathVariable Long logroId,
            @Valid @RequestBody RecompensaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(logroService.agregarRecompensa(logroId, request));
    }

    @DeleteMapping("/{logroId}/recompensas/{recompensaId}")
    public ResponseEntity<Void> quitarRecompensa(
            @PathVariable Long logroId,
            @PathVariable Long recompensaId) {
        logroService.quitarRecompensa(logroId, recompensaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<UsuarioLogroResponse> logrosDeUsuario(@PathVariable Long usuarioId) {
        return logroService.listarLogrosUsuario(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/recompensas")
    public List<UsuarioRecompensaResponse> recompensasDeUsuario(@PathVariable Long usuarioId) {
        return logroService.listarRecompensasUsuario(usuarioId);
    }

    @PostMapping("/usuario/{usuarioId}/recompensas/{recompensaId}/reclamar")
    public UsuarioRecompensaResponse reclamarRecompensa(
            @PathVariable Long usuarioId,
            @PathVariable Long recompensaId) {
        return logroService.reclamarRecompensa(usuarioId, recompensaId);
    }
}
