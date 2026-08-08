package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraResponse;
import org.example.lfmnacional.service.ResultadoCarreraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadoCarreraController {

    private final ResultadoCarreraService resultadoCarreraService;

    @PostMapping("/cargar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<List<ResultadoCarreraResponse>> cargarResultados(
            @Valid @RequestBody CargarResultadosRequest request) {
        List<ResultadoCarreraResponse> respuestas = resultadoCarreraService.cargarResultados(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestas);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<ResultadoCarreraResponse> listarPorCarrera(@PathVariable Long carreraId) {
        return resultadoCarreraService.listarPorCarrera(carreraId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ResultadoCarreraResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return resultadoCarreraService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/{id}")
    public ResultadoCarreraResponse getById(@PathVariable Long id) {
        return resultadoCarreraService.getById(id);
    }
}
