package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.archivo.ArchivoCarreraResponse;
import org.example.lfmnacional.entity.ArchivoCarrera;
import org.example.lfmnacional.enums.TipoArchivo;
import org.example.lfmnacional.service.ArchivoCarreraService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoCarreraController {

    private final ArchivoCarreraService archivoCarreraService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArchivoCarreraResponse> create(
            @RequestParam("nombre") String nombre,
            @RequestParam("tipo") TipoArchivo tipo,
            @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(archivoCarreraService.create(nombre, tipo, archivo));
    }

    @GetMapping
    public List<ArchivoCarreraResponse> listAll() {
        return archivoCarreraService.listAll();
    }

    @GetMapping("/{id}")
    public ArchivoCarreraResponse getById(@PathVariable Long id) {
        return archivoCarreraService.getById(id);
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) throws IOException {
        ArchivoCarrera archivo = archivoCarreraService.getEntity(id);
        Path ruta = archivoCarreraService.resolverRuta(archivo);
        Resource resource = new PathResource(ruta);

        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        String probado = Files.probeContentType(ruta);
        if (probado != null) {
            contentType = MediaType.parseMediaType(probado);
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(Files.size(ruta))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(archivo.getNombre(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        archivoCarreraService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
