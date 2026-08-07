package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.apelacion.ApelacionResolucionRequest;
import org.example.lfmnacional.dto.apelacion.ApelacionResponse;
import org.example.lfmnacional.service.ApelacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apelaciones")
@RequiredArgsConstructor
public class ApelacionController {

    private final ApelacionService apelacionService;

    @GetMapping
    public List<ApelacionResponse> listAll() {
        return apelacionService.listAll();
    }

    @GetMapping("/pendientes")
    public List<ApelacionResponse> listarPendientes() {
        return apelacionService.listarPendientes();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ApelacionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return apelacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/{id}")
    public ApelacionResponse getById(@PathVariable Long id) {
        return apelacionService.getById(id);
    }

    @PutMapping("/{id}/resolucion")
    public ApelacionResponse resolver(@PathVariable Long id, @Valid @RequestBody ApelacionResolucionRequest request) {
        return apelacionService.resolver(id, request);
    }
}
