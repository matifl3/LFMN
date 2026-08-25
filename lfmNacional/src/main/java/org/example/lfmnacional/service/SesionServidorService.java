package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesionServidorService {

    private static final String SESION_QUALIFY = "QUALIFY";
    private static final String SESION_RACE = "RACE";

    private final CarreraService carreraService;
    private final CarreraResolverService carreraResolverService;
    private final ClasificacionImportService clasificacionImportService;
    private final ResultadoImportService resultadoImportService;
    private final VueltaImportService vueltaImportService;
    private final IncidenteAutoGenService incidenteAutoGenService;
    private final SesionProcesadaRepository sesionProcesadaRepository;

    @Transactional
    public String importarSesion(Long carreraId, SesionServerData sesion) {
        Carrera carrera = carreraService.getEntity(carreraId);
        return importar(carrera, sesion);
    }

    private String importar(Carrera carrera, SesionServerData sesion) {
        if (sesion == null) {
            throw new BusinessException("El JSON de sesion es invalido");
        }
        String tipo = sesion.type() != null ? sesion.type().toUpperCase() : "";
        switch (tipo) {
            case SESION_QUALIFY -> clasificacionImportService.importarClasificacion(carrera, sesion);
            case SESION_RACE -> {
                resultadoImportService.importarResultados(carrera, sesion);
                try {
                    vueltaImportService.importarVueltas(carrera, sesion, tipo);
                } catch (Exception e) {
                    log.warn("Error al importar vueltas para carrera {}: {}", carrera.getNombre(), e.getMessage());
                }
                try {
                    incidenteAutoGenService.autogenerarIncidentes(carrera, sesion);
                } catch (Exception e) {
                    log.warn("Error al autogenerar incidentes para carrera {}: {}", carrera.getNombre(), e.getMessage());
                }
            }
            case "PRACTICE" -> log.info("Sesion PRACTICE ignorada (no importa datos)");
            default -> throw new BusinessException("Tipo de sesion no soportado: " + sesion.type());
        }
        return tipo;
    }

    @Transactional
    public void registrarProcesada(Long carreraId, String nombreArchivo, String tipo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        if (sesionProcesadaRepository.existsByNombreArchivo(nombreArchivo)) {
            return;
        }
        sesionProcesadaRepository.save(SesionProcesada.builder()
                .carrera(carrera)
                .nombreArchivo(nombreArchivo)
                .tipo(tipo)
                .build());
    }

    public boolean yaProcesada(String nombreArchivo) {
        return sesionProcesadaRepository.existsByNombreArchivo(nombreArchivo);
    }

    public Carrera resolverCarrera(SesionServerData sesion, java.time.LocalDateTime momentoSesion) {
        return carreraResolverService.resolverCarrera(sesion, momentoSesion);
    }
}
