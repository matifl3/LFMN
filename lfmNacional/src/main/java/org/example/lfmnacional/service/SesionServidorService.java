package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.example.lfmnacional.service.sesion.IdempotenciaSesionService;
import org.example.lfmnacional.service.sesion.ImportadorSesion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Ingesta de sesiones de servidor (JSON de Assetto Corsa).
 *
 * La ingesta es manual: un operador sube el JSON al endpoint
 * POST /api/sesiones/importar. Ya no hayEscaneo automatico de carpeta
 * (SesionFolderWatcher se elimino): la app no depende de disco local, que en
 * Render es efimero.
 */
@Service
@RequiredArgsConstructor
public class SesionServidorService {

    private final CarreraService carreraService;
    private final List<ImportadorSesion> importadores;
    private final SesionProcesadaRepository sesionProcesadaRepository;
    private final IdempotenciaSesionService idempotenciaSesionService;

    /**
     * Resultado de una importacion: indica si fue nueva o ya existia.
     */
    public record ResultadoImportacion(String tipo, String clave, boolean yaProcesada) {
    }

    /**
     * Importa una sesion de servidor. Idempotente por contenido: si el mismo JSON
     * ya fue importado para esa carrera, no reprocesa.
     *
     * @param nombreArchivo opcional, solo para trazabilidad en el historial.
     */
    @Transactional
    public ResultadoImportacion importarConIdempotencia(Long carreraId, SesionServerData sesion,
                                                         String nombreArchivo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        String clave = idempotenciaSesionService.claveSesion(carreraId, sesion);

        Optional<SesionProcesada> existente = sesionProcesadaRepository.findByClave(clave);
        if (existente.isPresent()) {
            return new ResultadoImportacion(existente.get().getTipo(), clave, true);
        }

        String tipo = importar(carrera, sesion);
        registrarProcesada(carrera, clave, nombreArchivo, tipo);
        return new ResultadoImportacion(tipo, clave, false);
    }

    private String importar(Carrera carrera, SesionServerData sesion) {
        if (sesion == null) {
            throw new BusinessException("El JSON de sesion es invalido");
        }
        String tipo = sesion.type() != null ? sesion.type().toUpperCase() : "";
        ImportadorSesion importador = importadores.stream()
                .filter(i -> i.tipo().equals(tipo))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tipo de sesion no soportado: " + sesion.type()));
        return importador.importar(carrera, sesion);
    }

    private void registrarProcesada(Carrera carrera, String clave, String nombreArchivo, String tipo) {
        sesionProcesadaRepository.save(SesionProcesada.builder()
                .carrera(carrera)
                .clave(clave)
                .nombreArchivo(nombreArchivo)
                .tipo(tipo)
                .build());
    }
}