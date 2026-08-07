package org.example.lfmnacional.config;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.service.SesionServidorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesionFolderWatcher {

    private static final String EXTENSION_JSON = ".json";

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
            if (sesionServidorService.yaProcesada(nombre)) {
                log.info("Sesion ya procesada, moviendo: {}", nombre);
                mover(archivo, procesadasDir);
                return;
            }
            Long carreraId = parseCarreraId(nombre);
            SesionServerData sesion = objectMapper.readValue(archivo.toFile(), SesionServerData.class);
            String tipo = sesionServidorService.importarSesion(carreraId, sesion);
            sesionServidorService.registrarProcesada(carreraId, nombre, tipo);
            mover(archivo, procesadasDir);
            log.info("Sesion {} procesada para la carrera {}", nombre, carreraId);
        } catch (Exception e) {
            log.error("Error procesando sesion {}", nombre, e);
            mover(archivo, erroresDir);
        }
    }

    private Long parseCarreraId(String nombre) {
        int guion = nombre.indexOf('_');
        if (guion <= 0) {
            throw new IllegalArgumentException("Nombre de archivo invalido, se espera <carreraId>_... : " + nombre);
        }
        try {
            return Long.parseLong(nombre.substring(0, guion));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CarreraId invalido en nombre de archivo: " + nombre);
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
