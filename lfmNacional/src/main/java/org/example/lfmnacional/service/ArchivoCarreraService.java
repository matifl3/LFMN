package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.archivo.ArchivoCarreraResponse;
import org.example.lfmnacional.entity.ArchivoCarrera;
import org.example.lfmnacional.enums.TipoArchivo;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ArchivoCarreraRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.example.lfmnacional.util.FileUtil;

@Service
@RequiredArgsConstructor
public class ArchivoCarreraService {

    private final ArchivoCarreraRepository archivoCarreraRepository;

    @Value("${archivos.base-dir}")
    private String baseDir;

    public ArchivoCarrera getEntity(Long id) {
        return archivoCarreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo de carrera no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "archivos_carrera", key = "#id")
    public ArchivoCarreraResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("archivos_carrera")
    public List<ArchivoCarreraResponse> listAll() {
        return archivoCarreraRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "archivos_carrera", allEntries = true)
    public ArchivoCarreraResponse create(String nombre, TipoArchivo tipo, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo no puede estar vacio");
        }
        String extension = FileUtil.obtenerExtension(archivo.getOriginalFilename());
        String nombreAlmacenado = UUID.randomUUID() + extension;
        Path destino = Paths.get(baseDir).toAbsolutePath().normalize().resolve(nombreAlmacenado);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar el archivo en el servidor");
        }
        ArchivoCarrera guardado = archivoCarreraRepository.save(ArchivoCarrera.builder()
                .nombre(nombre)
                .ruta(nombreAlmacenado)
                .tipo(tipo)
                .build());
        return toResponse(guardado);
    }

    @Transactional
    @CacheEvict(value = "archivos_carrera", allEntries = true)
    public void delete(Long id) {
        archivoCarreraRepository.delete(getEntity(id));
    }

    public Path resolverRuta(ArchivoCarrera archivo) {
        Path base = Paths.get(baseDir).toAbsolutePath().normalize();
        Path ruta = Paths.get(archivo.getRuta());
        Path resuelta = ruta.isAbsolute() ? ruta : base.resolve(ruta);
        resuelta = resuelta.normalize();
        if (!resuelta.startsWith(base)) {
            throw new BusinessException("La ruta del archivo queda fuera del directorio permitido");
        }
        if (Files.notExists(resuelta) || Files.isDirectory(resuelta)) {
            throw new ResourceNotFoundException("Archivo no encontrado en el servidor");
        }
        return resuelta;
    }

    private ArchivoCarreraResponse toResponse(ArchivoCarrera archivo) {
        return new ArchivoCarreraResponse(
                archivo.getId(),
                archivo.getNombre(),
                archivo.getRuta(),
                archivo.getTipo());
    }
}
