package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.RestablecerPasswordRequest;
import org.example.lfmnacional.dto.usuario.SolicitarRecuperacionRequest;
import org.example.lfmnacional.service.RecuperarPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recuperar")
@RequiredArgsConstructor
public class RecuperarController {

    private final RecuperarPasswordService recuperarPasswordService;

    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        recuperarPasswordService.solicitar(request.email());
        return ResponseEntity.ok(Map.of("mensaje", "Si el email existe, te enviamos un enlace de recuperación"));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(@Valid @RequestBody RestablecerPasswordRequest request) {
        recuperarPasswordService.restablecer(request.token(), request.nuevaPassword());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada. Ya podés iniciar sesión"));
    }
}