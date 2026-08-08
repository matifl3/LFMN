package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionProcesadaResponse;
import org.example.lfmnacional.service.SesionProcesadaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sesiones-procesadas")
@RequiredArgsConstructor
public class SesionProcesadaController {

    private final SesionProcesadaService sesionProcesadaService;

    @GetMapping
    public List<SesionProcesadaResponse> listAll() {
        return sesionProcesadaService.listAll();
    }

    @GetMapping("/{id}")
    public SesionProcesadaResponse getById(@PathVariable Long id) {
        return sesionProcesadaService.getById(id);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<SesionProcesadaResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return sesionProcesadaService.listarPorCarrera(carreraId);
    }
}
