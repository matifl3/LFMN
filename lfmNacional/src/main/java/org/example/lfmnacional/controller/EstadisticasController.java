package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.estadistica.EstadisticasResponse;
import org.example.lfmnacional.service.EstadisticasService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EstadisticasResponse getEstadisticas() {
        return estadisticasService.getEstadisticas();
    }
}