package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.SesionYaImportadaException;
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
     * reprocesar ({@code estado = YA_PROCESADA}). Un JSON distinto para una
     * sesion de ese tipo ya importada tambien es 409, con
     * {@code estado = OTRA_SESION_IMPORTADA}: se corrige con {@code /reimportar}.
     *
     * <p>Eso permite al cliente reintentar ante errores de red sin riesgo de
     * duplicar resultados ni incidentes.
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
        SesionServidorService.ResultadoImportacion resultado;
        try {
            resultado = sesionServidorService.importarConIdempotencia(carreraId, sesion, nombreArchivo);
        } catch (SesionYaImportadaException e) {
            // Otro contenido para una sesion ya importada: se corrige con /reimportar.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "carreraId", carreraId,
                            "tipo", e.getTipo(),
                            "estado", "OTRA_SESION_IMPORTADA",
                            "mensaje", e.getMessage()));
        }

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

    /**
     * Reemplaza una sesion ya importada por un JSON corregido: deshace los
     * resultados (y el Elo/SR y los puntos que dieron) o la clasificacion
     * anteriores y vuelve a importar. Ver {@code DeshacerSesionService}.
     *
     * <p>Solo ADMIN y COMISARIO: borra resultados y mueve Elo de varios pilotos.
     * Falla con 400 si la carrera tiene sanciones cargadas.
     */
    @PostMapping("/reimportar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO')")
    public ResponseEntity<Map<String, Object>> reimportar(@RequestParam Long carreraId,
                                                          @RequestBody SesionServerData sesion,
                                                          @RequestParam(required = false) String nombreArchivo) {
        SesionServidorService.ResultadoImportacion resultado =
                sesionServidorService.reimportar(carreraId, sesion, nombreArchivo);
        if (resultado.yaProcesada()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "carreraId", carreraId,
                            "tipo", resultado.tipo(),
                            "estado", "YA_PROCESADA"));
        }
        return ResponseEntity.ok(Map.of(
                "carreraId", carreraId,
                "tipo", resultado.tipo(),
                "estado", "REIMPORTADA"));
    }
}
