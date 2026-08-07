package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.CampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.CampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.service.CampeonatoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {

    private final CampeonatoService campeonatoService;

    @GetMapping
    public List<CampeonatoResponse> listAll() {
        return campeonatoService.listAll();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<CampeonatoResponse> porCategoria(@PathVariable Long categoriaId) {
        return campeonatoService.porCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public CampeonatoResponse getById(@PathVariable Long id) {
        return campeonatoService.getById(id);
    }

    @GetMapping("/{id}/tabla")
    public List<TablaPosicionResponse> getTabla(@PathVariable Long id) {
        return campeonatoService.getTabla(id);
    }

    @PostMapping
    public ResponseEntity<CampeonatoResponse> create(@Valid @RequestBody CampeonatoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campeonatoService.create(request));
    }

    @PutMapping("/{id}")
    public CampeonatoResponse update(@PathVariable Long id, @Valid @RequestBody CampeonatoRequest request) {
        return campeonatoService.update(id, request);
    }

    @PutMapping("/{id}/cerrar")
    public CampeonatoResponse cerrar(@PathVariable Long id) {
        return campeonatoService.cerrar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campeonatoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
