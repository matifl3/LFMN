package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupRequest;
import org.example.lfmnacional.dto.setup.SetupResponse;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.service.SetupService;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/setups")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping
    public List<SetupResponse> listAll() {
        return setupService.listAll();
    }

    @GetMapping("/buscar")
    public List<SetupResponse> buscar(
            @RequestParam(required = false) String circuito,
            @RequestParam(required = false) String vehiculo) {
        return setupService.buscar(circuito, vehiculo);
    }

    @GetMapping("/autor/{autorId}")
    public List<SetupResponse> listarPorAutor(@PathVariable Long autorId) {
        return setupService.listarPorAutor(autorId);
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<SetupResponse> listarPorCategoria(@PathVariable Long categoriaId) {
        return setupService.listarPorCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    public SetupResponse getById(@PathVariable Long id) {
        return setupService.getById(id);
    }

    @PostMapping
    public ResponseEntity<SetupResponse> create(@Valid @RequestBody SetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setupService.create(request));
    }

    @PostMapping("/{id}/archivo")
    public ResponseEntity<Map<String, String>> subirArchivo(
            @PathVariable Long id,
            @RequestPart("archivo") MultipartFile archivo) {
        setupService.guardarArchivo(id, archivo);
        return ResponseEntity.ok(Map.of("mensaje", "Archivo subido correctamente"));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) throws IOException {
        Setup setup = setupService.getEntity(id);
        Path ruta = setupService.resolverRuta(setup);
        Resource resource = new PathResource(ruta);
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        String probado = Files.probeContentType(ruta);
        if (probado != null) {
            contentType = MediaType.parseMediaType(probado);
        }
        String nombreOriginal = setup.getTitulo();
        String nombreArchivo = ruta.getFileName().toString();
        int punto = nombreArchivo.lastIndexOf('.');
        String ext = punto >= 0 ? nombreArchivo.substring(punto) : "";
        String nombreDescarga = nombreOriginal + ext;
        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(Files.size(ruta))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(nombreDescarga, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }

    @PutMapping("/{id}")
    public SetupResponse update(@PathVariable Long id, @Valid @RequestBody SetupRequest request) {
        return setupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        setupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
