package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.MiembroCampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.MiembroCampeonatoResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoMiembroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Roster de pilotos. La lectura exige ser miembro del campeonato (o su admin /
 * un comisario); sumar y quitar pilotos es solo del administrador del campeonato.
 * El gate fino de lectura vive en el service, asi que aca alcanza con pedir
 * autenticacion y dejar el control de escritura a nivel de metodo.
 */
@RestController
@RequestMapping("/api/campeonatos/{campeonatoId}/miembros")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CampeonatoMiembroController {

    private final CampeonatoMiembroService miembroService;

    @GetMapping
    public List<MiembroCampeonatoResponse> listar(@PathVariable Long campeonatoId,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return miembroService.listar(campeonatoId, usuario);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    @PostMapping
    public ResponseEntity<MiembroCampeonatoResponse> agregar(@PathVariable Long campeonatoId,
                                                           @Valid @RequestBody MiembroCampeonatoRequest request,
                                                           @AuthenticationPrincipal Usuario usuario) {
        MiembroCampeonatoResponse response = miembroService.agregar(campeonatoId, request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN_CAMPEONATO')")
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> quitar(@PathVariable Long campeonatoId,
                                       @PathVariable Long usuarioId,
                                       @AuthenticationPrincipal Usuario usuario) {
        miembroService.quitar(campeonatoId, usuarioId, usuario);
        return ResponseEntity.noContent().build();
    }
}
