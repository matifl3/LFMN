package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaAnalisisResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResumenResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.VueltaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vueltas")
@RequiredArgsConstructor
public class VueltaController {

    private final VueltaService vueltaService;

    @GetMapping("/carrera/{carreraId}")
    public List<VueltaResponse> listarPorCarrera(@PathVariable Long carreraId,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return vueltaService.listarPorCarrera(carreraId, usuario);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}")
    public List<VueltaResponse> listarPorUsuario(@PathVariable Long carreraId,
                                                 @PathVariable Long usuarioId,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return vueltaService.listarPorUsuarioEnCarrera(carreraId, usuarioId, usuario);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}/analisis")
    public List<VueltaAnalisisResponse> analisisPorUsuario(@PathVariable Long carreraId,
                                                           @PathVariable Long usuarioId,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return vueltaService.analisisCarrera(carreraId, usuarioId, usuario);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}/analisis/resumen")
    public VueltaResumenResponse resumenPorUsuario(@PathVariable Long carreraId,
                                                   @PathVariable Long usuarioId,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return vueltaService.resumenCarrera(carreraId, usuarioId, usuario);
    }
}
