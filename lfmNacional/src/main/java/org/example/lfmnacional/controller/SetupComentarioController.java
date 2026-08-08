package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupComentarioRequest;
import org.example.lfmnacional.dto.setup.SetupComentarioResponse;
import org.example.lfmnacional.service.SetupComentarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setups/{setupId}/comentarios")
@RequiredArgsConstructor
public class SetupComentarioController {

    private final SetupComentarioService setupComentarioService;

    @GetMapping
    public List<SetupComentarioResponse> listarPorSetup(@PathVariable Long setupId) {
        return setupComentarioService.listarPorSetup(setupId);
    }

    @PostMapping
    public ResponseEntity<SetupComentarioResponse> create(
            @PathVariable Long setupId,
            @Valid @RequestBody SetupComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(setupComentarioService.create(setupId, request));
    }

    @PutMapping("/{comentarioId}")
    public SetupComentarioResponse update(
            @PathVariable Long comentarioId,
            @Valid @RequestBody SetupComentarioRequest request) {
        return setupComentarioService.update(comentarioId, request);
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> delete(@PathVariable Long comentarioId) {
        setupComentarioService.delete(comentarioId);
        return ResponseEntity.noContent().build();
    }
}
