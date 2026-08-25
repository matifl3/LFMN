package org.example.lfmnacional.service;

import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.example.lfmnacional.util.FileUtil;

@Slf4j
@Service
public class ImagenService {

    private static final String SUBDIRECTORIO = "imagenes";
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    @Value("${archivos.base-dir}")
    private String baseDir;

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo no puede estar vacio");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new BusinessException("Tipo de archivo no permitido. Se aceptan: JPG, PNG, GIF, WebP");
        }
        String extension = FileUtil.obtenerExtension(archivo.getOriginalFilename());
        String nombreAlmacenado = UUID.randomUUID() + extension;
        Path destino = Paths.get(baseDir, SUBDIRECTORIO, nombreAlmacenado).toAbsolutePath().normalize();
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            throw new BusinessException("No se pudo guardar la imagen en el servidor");
        }
        return "/api/imagenes/" + nombreAlmacenado;
    }

    public Path resolverRuta(String nombre) {
        Path base = Paths.get(baseDir, SUBDIRECTORIO).toAbsolutePath().normalize();
        Path resuelta = base.resolve(nombre).normalize();
        if (!resuelta.startsWith(base)) {
            throw new BusinessException("Ruta de imagen no valida");
        }
        if (Files.notExists(resuelta) || Files.isDirectory(resuelta)) {
            throw new BusinessException("Imagen no encontrada");
        }
        return resuelta;
    }
}
