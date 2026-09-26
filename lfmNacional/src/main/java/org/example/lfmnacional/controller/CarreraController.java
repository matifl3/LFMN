package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.carrera.CarreraAccesoResponse;
import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.dto.carrera.CarreraResponse;
import org.example.lfmnacional.dto.carrera.EloEstimadoResponse;
import org.example.lfmnacional.dto.carrera.VincularArchivoRequest;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CampeonatoService;
import org.example.lfmnacional.service.CarreraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {

    private final CarreraService carreraService;
    private final CampeonatoService campeonatoService;
    private final CampeonatoAccesoService accesoService;

    @GetMapping
    public PageResponse<CarreraResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @AuthenticationPrincipal Usuario usuario) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        Page<CarreraResponse> result = carreraService.listAll(pageable, usuario);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/proximas")
    public List<CarreraResponse> proximas(@AuthenticationPrincipal Usuario usuario) {
        return carreraService.proximas(usuario);
    }

    @GetMapping("/pasadas")
    public List<CarreraResponse> pasadas(@AuthenticationPrincipal Usuario usuario) {
        return carreraService.pasadas(usuario);
    }

    @GetMapping("/campeonato/{campeonatoId}")
    public List<CarreraResponse> porCampeonato(@PathVariable Long campeonatoId,
                                               @AuthenticationPrincipal Usuario usuario) {
        return carreraService.porCampeonato(campeonatoId, usuario);
    }

    @GetMapping("/{id}")
    public CarreraResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return carreraService.getById(id, usuario);
    }

    @GetMapping("/{id}/acceso-servidor")
    public CarreraAccesoResponse accesoServidor(@PathVariable Long id,
                                                @AuthenticationPrincipal Usuario usuario) {
        return carreraService.accesoServidor(id, usuario != null ? usuario.getId() : null);
    }

    @GetMapping("/{id}/elo-estimado")
    public EloEstimadoResponse eloEstimado(@PathVariable Long id,
                                           @AuthenticationPrincipal Usuario usuario) {
        return carreraService.eloEstimado(id, usuario != null ? usuario.getId() : null);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<CarreraResponse> create(@Valid @RequestBody CarreraRequest request,
                                                  @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirAdministra(usuario, campeonatoService.getEntity(request.campeonatoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CarreraResponse update(@PathVariable Long id, @Valid @RequestBody CarreraRequest request,
                                  @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeCarreraYDelCampeonatoDestino(id, request, usuario);
        return carreraService.update(id, request);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CarreraResponse changeEstado(@PathVariable Long id, @RequestParam EstadoCarrera estado,
                                        @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        return carreraService.changeEstado(id, estado);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CarreraResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        return carreraService.cancelar(id);
    }

    @PutMapping("/{id}/archivo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CarreraResponse vincularArchivo(@PathVariable Long id,
                                           @Valid @RequestBody VincularArchivoRequest request,
                                           @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        return carreraService.vincularArchivo(id, request.archivoId());
    }

    @DeleteMapping("/{id}/archivo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public CarreraResponse desvincularArchivo(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        return carreraService.desvincularArchivo(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        carreraService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Al mover una carrera de campeonato hay que poder administrar los dos: el
     * de origen (para sacarla) y el de destino (para ponerla).
     */
    private void exigirAdminDeCarreraYDelCampeonatoDestino(Long id, CarreraRequest request,
                                                                               Usuario usuario) {
        exigirAdminDeLaCarrera(id, usuario);
        accesoService.exigirAdministra(usuario, campeonatoService.getEntity(request.campeonatoId()));
    }

    private void exigirAdminDeLaCarrera(Long id, Usuario usuario) {
        accesoService.exigirAdministraCarrera(usuario, carreraService.getEntity(id));
    }
}
