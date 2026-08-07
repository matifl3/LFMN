package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.anuncio.AnuncioRequest;
import org.example.lfmnacional.dto.anuncio.AnuncioResponse;
import org.example.lfmnacional.service.AnuncioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final AnuncioService anuncioService;

    @GetMapping
    public List<AnuncioResponse> listAll() {
        return anuncioService.listAll();
    }

    @GetMapping("/ultimo")
    public AnuncioResponse getUltimo() {
        return anuncioService.getUltimo();
    }

    @GetMapping("/{id}")
    public AnuncioResponse getById(@PathVariable Long id) {
        return anuncioService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AnuncioResponse> create(@Valid @RequestBody AnuncioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anuncioService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        anuncioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
