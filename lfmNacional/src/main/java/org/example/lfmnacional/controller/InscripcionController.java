package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.inscripcion.InscripcionRequest;
import org.example.lfmnacional.dto.inscripcion.InscripcionResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.InscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final CampeonatoAccesoService accesoService;

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribirse(@AuthenticationPrincipal Usuario actual,
                                                           @Valid @RequestBody InscripcionRequest request) {
        InscripcionResponse response = inscripcionService.inscribirse(request.carreraId(), actual.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<InscripcionResponse> baja(@PathVariable Long id,
                                                    @AuthenticationPrincipal Usuario actual) {
        Inscripcion inscripcion = inscripcionService.getEntity(id);
        boolean esPropia = inscripcion.getUsuario().getId().equals(actual.getId());
        if (!esPropia && !puedeAdministrarCarrera(actual, inscripcion.getCarrera())) {
            throw new BusinessException("No tenes permiso para cancelar la inscripcion de otro usuario");
        }
        return ResponseEntity.ok(inscripcionService.baja(id));
    }

    @DeleteMapping("/carrera/{carreraId}/usuario/{usuarioId}")
    public ResponseEntity<InscripcionResponse> cancelar(@PathVariable Long carreraId,
                                                        @PathVariable Long usuarioId,
                                                        @AuthenticationPrincipal Usuario actual) {
        boolean esPropia = actual.getId().equals(usuarioId);
        if (!esPropia && !puedeAdministrarCarrera(actual, inscripcionService.carrera(carreraId))) {
            throw new BusinessException("No tenes permiso para cancelar la inscripcion de otro usuario");
        }
        return ResponseEntity.ok(inscripcionService.cancelar(carreraId, usuarioId));
    }

    /** ADMIN global o el dueno del campeonato al que pertenece la carrera. */
    private boolean puedeAdministrarCarrera(Usuario actual, Carrera carrera) {
        if (accesoService.esAdminGlobal(actual)) {
            return true;
        }
        return accesoService.administraCarrera(actual, carrera);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<InscripcionResponse> listarPorCarrera(@PathVariable Long carreraId,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return inscripcionService.listarPorCarrera(carreraId, usuario);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<InscripcionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return inscripcionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/carrera/{carreraId}/count")
    public Map<String, Long> countInscriptos(@PathVariable Long carreraId,
                                             @AuthenticationPrincipal Usuario usuario) {
        return Map.of("inscriptos", inscripcionService.countInscriptos(carreraId, usuario));
    }
}
