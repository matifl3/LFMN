package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.vuelta.VueltaResponse;
import org.example.lfmnacional.service.VueltaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vueltas")
@RequiredArgsConstructor
public class VueltaController {

    private final VueltaService vueltaService;

    @GetMapping("/carrera/{carreraId}")
    public List<VueltaResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return vueltaService.listarPorCarrera(carreraId);
    }

    @GetMapping("/carrera/{carreraId}/usuario/{usuarioId}")
    public List<VueltaResponse> listarPorUsuario(@PathVariable Long carreraId,
                                                 @PathVariable Long usuarioId) {
        return vueltaService.listarPorUsuarioEnCarrera(carreraId, usuarioId);
    }
}
