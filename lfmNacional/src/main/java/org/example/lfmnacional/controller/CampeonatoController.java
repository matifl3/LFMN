package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.CampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.CampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CampeonatoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {

    private final CampeonatoService campeonatoService;
    private final CampeonatoAccesoService accesoService;

    @GetMapping
    public List<CampeonatoResponse> listAll(@AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.listAll(usuario);
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<CampeonatoResponse> porCategoria(@PathVariable Long categoriaId,
                                                @AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.porCategoria(categoriaId, usuario);
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public List<CampeonatoResponse> misCampeonatos(@AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.misCampeonatos(usuario);
    }

    @GetMapping("/mis-membresias")
    public List<CampeonatoResponse> misMembresias(@AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.misMembresias(usuario);
    }

    @GetMapping("/{id}")
    public CampeonatoResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.getById(id, usuario);
    }

    @GetMapping("/{id}/tabla")
    public List<TablaPosicionResponse> getTabla(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        Campeonato campeonato = campeonatoService.getEntity(id);
        accesoService.exigirVeContenido(usuario, campeonato);
        return campeonatoService.getTabla(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<CampeonatoResponse> create(@Valid @RequestBody CampeonatoRequest request,
                                                    @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campeonatoService.create(request, usuario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CampeonatoResponse update(@PathVariable Long id, @Valid @RequestBody CampeonatoRequest request,
                                     @AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.update(id, request, usuario);
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CampeonatoResponse cerrar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return campeonatoService.cerrar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        campeonatoService.delete(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
