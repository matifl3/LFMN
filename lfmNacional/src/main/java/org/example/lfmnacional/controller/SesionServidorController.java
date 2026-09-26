package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CarreraService;
import org.example.lfmnacional.service.SesionServidorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionServidorController {

    private final SesionServidorService sesionServidorService;
    private final CarreraService carreraService;
    private final CampeonatoAccesoService accesoService;

    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam Long carreraId,
                                                        @RequestBody SesionServerData sesion,
                                                        @AuthenticationPrincipal Usuario usuario) {
        if (!accesoService.esComisario(usuario)) {
            accesoService.exigirAdministraCarrera(usuario, carreraService.getEntity(carreraId));
        }
        String tipo = sesionServidorService.importarSesion(carreraId, sesion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("carreraId", carreraId, "tipo", tipo, "estado", "PROCESADA"));
    }
}
