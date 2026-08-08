package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.RecompensaRequest;
import org.example.lfmnacional.dto.logro.RecompensaResponse;
import org.example.lfmnacional.dto.logro.UsuarioRecompensaResponse;
import org.example.lfmnacional.service.RecompensaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompensas")
@RequiredArgsConstructor
public class RecompensaController {

    private final RecompensaService recompensaService;

    @GetMapping
    public List<RecompensaResponse> listAll() {
        return recompensaService.listAll();
    }

    @GetMapping("/{id}")
    public RecompensaResponse getById(@PathVariable Long id) {
        return recompensaService.getById(id);
    }

    @GetMapping("/logro/{logroId}")
    public List<RecompensaResponse> listarPorLogro(@PathVariable Long logroId) {
        return recompensaService.listarPorLogro(logroId);
    }

    @GetMapping("/usuario/{usuarioId}/recompensas")
    public List<UsuarioRecompensaResponse> listarRecompensasUsuario(@PathVariable Long usuarioId) {
        return recompensaService.listarRecompensasUsuario(usuarioId);
    }

    @PostMapping("/logro/{logroId}")
    public ResponseEntity<RecompensaResponse> create(
            @PathVariable Long logroId,
            @Valid @RequestBody RecompensaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recompensaService.create(logroId, request));
    }

    @PutMapping("/{id}")
    public RecompensaResponse update(@PathVariable Long id, @Valid @RequestBody RecompensaRequest request) {
        return recompensaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recompensaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuario/{usuarioId}/recompensas/{recompensaId}/reclamar")
    public UsuarioRecompensaResponse reclamarRecompensa(
            @PathVariable Long usuarioId,
            @PathVariable Long recompensaId) {
        return recompensaService.reclamarRecompensa(usuarioId, recompensaId);
    }
}
