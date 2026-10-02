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

    /**
     * Importa el JSON de una sesion de Assetto Corsa.
     *
     * Idempotente por contenido: reenviar el mismo JSON devuelve 409 sin
     * reprocesar. Eso permite al cliente reintentar ante errores de red sin
     * riesgo de duplicar resultados ni incidentes.
     *
     * @param nombreArchivo opcional, solo para trazabilidad en el historial.
     */
    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam Long carreraId,
                                                        @RequestBody SesionServerData sesion,
                                                        @RequestParam(required = false) String nombreArchivo,
                                                        @AuthenticationPrincipal Usuario usuario) {
        if (!accesoService.esComisario(usuario)) {
            accesoService.exigirAdministraCarrera(usuario, carreraService.getEntity(carreraId));
        }
        SesionServidorService.ResultadoImportacion resultado =
                sesionServidorService.importarConIdempotencia(carreraId, sesion, nombreArchivo);

        if (resultado.yaProcesada()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "carreraId", carreraId,
                            "tipo", resultado.tipo(),
                            "estado", "YA_PROCESADA"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "carreraId", carreraId,
                        "tipo", resultado.tipo(),
                        "estado", "PROCESADA"));
    }
}