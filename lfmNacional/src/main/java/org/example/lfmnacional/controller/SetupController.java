package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupRequest;
import org.example.lfmnacional.dto.setup.SetupResponse;
import org.example.lfmnacional.service.SetupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setups")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping
    public List<SetupResponse> listAll() {
        return setupService.listAll();
    }

    @GetMapping("/buscar")
    public List<SetupResponse> buscar(
            @RequestParam(required = false) String circuito,
            @RequestParam(required = false) String vehiculo) {
        return setupService.buscar(circuito, vehiculo);
    }

    @GetMapping("/autor/{autorId}")
    public List<SetupResponse> listarPorAutor(@PathVariable Long autorId) {
        return setupService.listarPorAutor(autorId);
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<SetupResponse> listarPorCategoria(@PathVariable Long categoriaId) {
        return setupService.listarPorCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public SetupResponse getById(@PathVariable Long id) {
        return setupService.getById(id);
    }

    @PostMapping
    public ResponseEntity<SetupResponse> create(@Valid @RequestBody SetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setupService.create(request));
    }

    @PutMapping("/{id}")
    public SetupResponse update(@PathVariable Long id, @Valid @RequestBody SetupRequest request) {
        return setupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        setupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
