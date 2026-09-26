package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaAnalisisResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResumenResponse;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CarreraService;
import org.example.lfmnacional.service.VueltaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vueltas")
@RequiredArgsConstructor
public class VueltaController {

    private final VueltaService vueltaService;
    private final CarreraService carreraService;
    private final CampeonatoAccesoService accesoService;

    @GetMapping("/carrera/{carreraId}")
    public List<VueltaResponse> listarPorCarrera(@PathVariable Long carreraId,
                                                 @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carrera(carreraId));
        return vueltaService.listarPorCarrera(carreraId);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}")
    public List<VueltaResponse> listarPorUsuario(@PathVariable Long carreraId,
                                                 @PathVariable Long usuarioId,
                                                 @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carrera(carreraId));
        return vueltaService.listarPorUsuarioEnCarrera(carreraId, usuarioId);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}/analisis")
    public List<VueltaAnalisisResponse> analisisPorUsuario(@PathVariable Long carreraId,
                                                           @PathVariable Long usuarioId,
                                                           @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carrera(carreraId));
        return vueltaService.analisisCarrera(carreraId, usuarioId);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}/analisis/resumen")
    public VueltaResumenResponse resumenPorUsuario(@PathVariable Long carreraId,
                                                   @PathVariable Long usuarioId,
                                                   @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carrera(carreraId));
        return vueltaService.resumenCarrera(carreraId, usuarioId);
    }

    private org.example.lfmnacional.entity.Carrera carrera(Long carreraId) {
        return carreraService.getEntity(carreraId);
    }
}
