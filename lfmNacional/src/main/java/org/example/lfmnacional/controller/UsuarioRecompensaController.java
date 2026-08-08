package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.UsuarioRecompensaResponse;
import org.example.lfmnacional.service.UsuarioRecompensaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/recompensas")
@RequiredArgsConstructor
public class UsuarioRecompensaController {

    private final UsuarioRecompensaService usuarioRecompensaService;

    @GetMapping
    public List<UsuarioRecompensaResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return usuarioRecompensaService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/no-reclamadas")
    public List<UsuarioRecompensaResponse> listarNoReclamadas(@PathVariable Long usuarioId) {
        return usuarioRecompensaService.listarNoReclamadas(usuarioId);
    }

    @GetMapping("/{id}")
    public UsuarioRecompensaResponse getById(@PathVariable Long id) {
        return usuarioRecompensaService.getById(id);
    }
}
