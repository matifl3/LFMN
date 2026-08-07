package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.inscripcion.InscripcionRequest;
import org.example.lfmnacional.dto.inscripcion.InscripcionResponse;
import org.example.lfmnacional.service.InscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribirse(@Valid @RequestBody InscripcionRequest request) {
        InscripcionResponse response = inscripcionService.inscribirse(request.carreraId(), request.usuarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<InscripcionResponse> baja(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.baja(id));
    }

    @DeleteMapping("/carrera/{carreraId}/usuario/{usuarioId}")
    public ResponseEntity<InscripcionResponse> cancelar(@PathVariable Long carreraId, @PathVariable Long usuarioId) {
        return ResponseEntity.ok(inscripcionService.cancelar(carreraId, usuarioId));
    }

    @GetMapping("/carrera/{carreraId}")
    public List<InscripcionResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return inscripcionService.listarPorCarrera(carreraId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<InscripcionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return inscripcionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/carrera/{carreraId}/count")
    public Map<String, Long> countInscriptos(@PathVariable Long carreraId) {
        return Map.of("inscriptos", inscripcionService.countInscriptos(carreraId));
    }
}
