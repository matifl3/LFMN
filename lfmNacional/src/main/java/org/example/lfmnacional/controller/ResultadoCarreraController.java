package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraResponse;
import org.example.lfmnacional.service.ResultadoCarreraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PageResponse<ResultadoCarreraResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("carrera.fecha").descending());
        Page<ResultadoCarreraResponse> result = resultadoCarreraService.listarPorUsuario(usuarioId, pageable);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/{id}")
    public ResultadoCarreraResponse getById(@PathVariable Long id) {
        return resultadoCarreraService.getById(id);
    }
}
