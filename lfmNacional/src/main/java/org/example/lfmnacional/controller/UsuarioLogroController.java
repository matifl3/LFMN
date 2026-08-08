package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.UsuarioLogroResponse;
import org.example.lfmnacional.service.UsuarioLogroService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/logros")
@RequiredArgsConstructor
public class UsuarioLogroController {

    private final UsuarioLogroService usuarioLogroService;

    @GetMapping
    public List<UsuarioLogroResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return usuarioLogroService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/obtenidos")
    public List<UsuarioLogroResponse> listarObtenidos(@PathVariable Long usuarioId) {
        return usuarioLogroService.listarObtenidos(usuarioId);
    }

    @GetMapping("/{logroId}")
    public UsuarioLogroResponse obtenerProgreso(@PathVariable Long usuarioId, @PathVariable Long logroId) {
        return usuarioLogroService.obtenerProgreso(usuarioId, logroId);
    }
}
