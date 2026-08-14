package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.*;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

    @GetMapping
    public List<UsuarioResponse> listAll() {
        return usuarioService.listAll();
    }

    @GetMapping("/{id}")
    public UsuarioResponse getById(@PathVariable Long id) {
        return usuarioService.getById(id);
    }

    @PutMapping("/{id}/perfil")
    public UsuarioResponse updatePerfil(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.updatePerfil(id, request);
    }

    @PutMapping("/{id}/password")
    public void updatePassword(@PathVariable Long id, @Valid @RequestBody CambioPasswordRequest request) {
        usuarioService.updatePassword(id, request);
    }

    @PutMapping("/{id}/steam")
    public UsuarioResponse vincularSteam(@PathVariable Long id, @Valid @RequestBody SteamRequest request) {
        return usuarioService.vincularSteam(id, request);
    }

    @DeleteMapping("/{id}/steam")
    public UsuarioResponse desvincularSteam(@PathVariable Long id) {
        return usuarioService.desvincularSteam(id);
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse changeRol(@PathVariable Long id, @RequestParam Rol rol) {
        return usuarioService.changeRol(id, rol);
    }

    @PutMapping("/{id}/rating")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse updateRating(@PathVariable Long id, @Valid @RequestBody RatingRequest request) {
        return usuarioService.updateRating(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public StatsResponse getStats(@PathVariable Long id) {
        return usuarioService.getStats(id);
    }

    @GetMapping("/{id}/historial-elo")
    public List<EloHistorialResponse> getHistorialElo(@PathVariable Long id) {
        return usuarioService.getHistorialElo(id);
    }

    @GetMapping("/{id}/historial-safety-rating")
    public List<SafetyRatingHistorialResponse> getHistorialSafetyRating(@PathVariable Long id) {
        return usuarioService.getHistorialSafetyRating(id);
    }
}
