package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.dto.carrera.VincularArchivoRequest;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.service.CarreraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {

    private final CarreraService carreraService;

    @GetMapping
    public List<CarreraResponse> listAll() {
        return carreraService.listAll();
    }

    @GetMapping("/proximas")
    public List<CarreraResponse> proximas() {
        return carreraService.proximas();
    }

    @GetMapping("/pasadas")
    public List<CarreraResponse> pasadas() {
        return carreraService.pasadas();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<CarreraResponse> porCategoria(@PathVariable Long categoriaId) {
        return carreraService.porCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public CarreraResponse getById(@PathVariable Long id) {
        return carreraService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarreraResponse> create(@Valid @RequestBody CarreraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CarreraResponse update(@PathVariable Long id, @Valid @RequestBody CarreraRequest request) {
        return carreraService.update(id, request);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public CarreraResponse changeEstado(@PathVariable Long id, @RequestParam EstadoCarrera estado) {
        return carreraService.changeEstado(id, estado);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public CarreraResponse cancelar(@PathVariable Long id) {
        return carreraService.cancelar(id);
    }

    @PutMapping("/{id}/archivo")
    @PreAuthorize("hasRole('ADMIN')")
    public CarreraResponse vincularArchivo(@PathVariable Long id,
                                           @Valid @RequestBody VincularArchivoRequest request) {
        return carreraService.vincularArchivo(id, request.archivoId());
    }

    @DeleteMapping("/{id}/archivo")
    @PreAuthorize("hasRole('ADMIN')")
    public CarreraResponse desvincularArchivo(@PathVariable Long id) {
        return carreraService.desvincularArchivo(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carreraService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
