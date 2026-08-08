package org.example.lfmnacional.config;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.service.SesionServidorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesionFolderWatcher {

    private static final String EXTENSION_JSON = ".json";
    private static final Duration MINIMA_ANTIGUEDAD = Duration.ofSeconds(3);

    private final SesionServidorService sesionServidorService;
    private final ObjectMapper objectMapper;

    @Value("${sesiones.input-dir}")
    private String inputDir;

    @Value("${sesiones.procesadas-dir}")
    private String procesadasDir;

    @Value("${sesiones.errores-dir}")
    private String erroresDir;

    @Scheduled(fixedDelay = 10000, initialDelay = 15000)
    public void escanearCarpeta() {
        Path carpeta = Path.of(inputDir);
        if (!Files.isDirectory(carpeta)) {
            return;
        }
        try (Stream<Path> archivos = Files.list(carpeta)) {
            archivos
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(EXTENSION_JSON))
                    .sorted()
                    .forEach(this::procesarArchivo);
        } catch (IOException e) {
            log.error("Error al listar la carpeta de sesiones {}", inputDir, e);
        }
    }

    private void procesarArchivo(Path archivo) {
        String nombre = archivo.getFileName().toString();
        try {
            if (esDemasiadoReciente(archivo)) {
                log.info("Sesion aun escribiendose, se reintentara en el proximo ciclo: {}", nombre);
                return;
            }
            if (sesionServidorService.yaProcesada(nombre)) {
                log.info("Sesion ya procesada, moviendo: {}", nombre);
                mover(archivo, procesadasDir);
                return;
            }
            SesionServerData sesion = objectMapper.readValue(archivo.toFile(), SesionServerData.class);
            LocalDateTime momentoSesion = parseMomentoSesion(nombre, archivo);
            Carrera carrera = sesionServidorService.resolverCarrera(sesion, momentoSesion);
            String tipo = sesionServidorService.importarSesion(carrera.getId(), sesion);
            sesionServidorService.registrarProcesada(carrera.getId(), nombre, tipo);
            mover(archivo, procesadasDir);
            log.info("Sesion {} procesada para la carrera {}", nombre, carrera.getId());
        } catch (Exception e) {
            log.error("Error procesando sesion {}", nombre, e);
            mover(archivo, erroresDir);
        }
    }

    private boolean esDemasiadoReciente(Path archivo) {
        try {
            FileTime modificado = Files.getLastModifiedTime(archivo);
            return System.currentTimeMillis() - modificado.toMillis() < MINIMA_ANTIGUEDAD.toMillis();
        } catch (IOException e) {
            return true;
        }
    }

    private LocalDateTime parseMomentoSesion(String nombre, Path archivo) {
        String base = nombre.substring(0, nombre.lastIndexOf('.'));
        String[] partes = base.split("_");
        try {
            if (partes.length >= 5) {
                return LocalDateTime.of(
                        Integer.parseInt(partes[0]),
                        Integer.parseInt(partes[1]),
                        Integer.parseInt(partes[2]),
                        Integer.parseInt(partes[3]),
                        Integer.parseInt(partes[4]));
            }
        } catch (NumberFormatException | DateTimeException ignored) {
        }
        try {
            return LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(archivo).toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            return LocalDateTime.now();
        }
    }

    private void mover(Path archivo, String destinoDir) {
        try {
            Files.createDirectories(Path.of(destinoDir));
            Path destino = Path.of(destinoDir).resolve(archivo.getFileName());
            Files.move(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error moviendo {} a {}", archivo, destinoDir, e);
        }
    }
}
