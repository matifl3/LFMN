package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.anuncio.AnuncioResponse;
import org.example.lfmnacional.service.AnuncioService;
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
}
