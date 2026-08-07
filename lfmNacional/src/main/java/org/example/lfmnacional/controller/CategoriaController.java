package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.categoria.CategoriaRequest;
import org.example.lfmnacional.dto.categoria.CategoriaResponse;
import org.example.lfmnacional.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaResponse> listAll() {
        return categoriaService.listAll();
    }

    @GetMapping("/disponibles")
    public List<CategoriaResponse> disponiblesPorElo(@RequestParam Integer elo) {
        return categoriaService.disponiblesPorElo(elo);
    }

    @GetMapping("/{id}")
    public CategoriaResponse getById(@PathVariable Long id) {
        return categoriaService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.create(request));
    }

    @PutMapping("/{id}")
    public CategoriaResponse update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return categoriaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
