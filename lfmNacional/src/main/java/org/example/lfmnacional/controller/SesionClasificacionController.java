package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionRequest;
import org.example.lfmnacional.dto.clasificacion.SesionClasificacionResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CarreraService;
import org.example.lfmnacional.service.SesionClasificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clasificaciones")
@RequiredArgsConstructor
public class SesionClasificacionController {

    private final SesionClasificacionService sesionClasificacionService;
    private final CarreraService carreraService;
    private final CampeonatoAccesoService accesoService;

    @GetMapping
    public List<SesionClasificacionResponse> listAll(@AuthenticationPrincipal Usuario usuario) {
        return sesionClasificacionService.listAll(usuario);
    }

    @GetMapping("/{id}")
    public SesionClasificacionResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        var entidad = sesionClasificacionService.getEntity(id);
        accesoService.exigirVeCarrera(usuario, entidad.getCarrera());
        return sesionClasificacionService.getById(id);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<SesionClasificacionResponse> listarPorCarrera(@PathVariable Long carreraId,
                                                              @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carreraService.getEntity(carreraId));
        return sesionClasificacionService.listarPorCarrera(carreraId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<SesionClasificacionResponse> listarPorUsuario(@PathVariable Long usuarioId,
                                                             @AuthenticationPrincipal Usuario usuario) {
        return sesionClasificacionService.listarPorUsuario(usuarioId, usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<SesionClasificacionResponse> create(
            @Valid @RequestBody SesionClasificacionRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(request.carreraId(), usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(sesionClasificacionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public SesionClasificacionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SesionClasificacionRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(sesionClasificacionService.getEntity(id).getCarrera().getId(), usuario);
        exigirAdminDeLaCarrera(request.carreraId(), usuario);
        return sesionClasificacionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(sesionClasificacionService.getEntity(id).getCarrera().getId(), usuario);
        sesionClasificacionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void exigirAdminDeLaCarrera(Long carreraId, Usuario usuario) {
        if (accesoService.esComisario(usuario)) {
            return;
        }
        accesoService.exigirAdministraCarrera(usuario, carreraService.getEntity(carreraId));
    }
}
