package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.service.ImagenService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenService imagenService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> subir(@RequestPart("archivo") MultipartFile archivo) {
        String url = imagenService.guardar(archivo);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Resource> servir(@PathVariable String nombre) throws IOException {
        Path ruta = imagenService.resolverRuta(nombre);
        Resource resource = new PathResource(ruta);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        String probado = Files.probeContentType(ruta);
        if (probado != null) {
            contentType = MediaType.parseMediaType(probado);
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }
}
