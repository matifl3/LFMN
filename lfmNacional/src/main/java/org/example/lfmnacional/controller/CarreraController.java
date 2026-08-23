package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.dto.carrera.VincularArchivoRequest;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.service.CarreraService;
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
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {

    private final CarreraService carreraService;

    @GetMapping
    public PageResponse<CarreraResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        Page<CarreraResponse> result = carreraService.listAll(pageable);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/proximas")
    public List<CarreraResponse> proximas() {
        return carreraService.proximas();
    }

    @GetMapping("/pasadas")
    public List<CarreraResponse> pasadas() {
        return carreraService.pasadas();
    }

    @GetMapping("/campeonato/{campeonatoId}")
    public List<CarreraResponse> porCampeonato(@PathVariable Long campeonatoId) {
        return carreraService.porCampeonato(campeonatoId);
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
