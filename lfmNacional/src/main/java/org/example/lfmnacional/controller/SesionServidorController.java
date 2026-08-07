package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.service.SesionServidorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionServidorController {

    private final SesionServidorService sesionServidorService;

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam Long carreraId,
                                                        @RequestBody SesionServerData sesion) {
        String tipo = sesionServidorService.importarSesion(carreraId, sesion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("carreraId", carreraId, "tipo", tipo, "estado", "PROCESADA"));
    }
}
