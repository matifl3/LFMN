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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SesionServidorService {

    private final CarreraService carreraService;
    private final CarreraResolverService carreraResolverService;
    private final List<ImportadorSesion> importadores;
    private final SesionProcesadaRepository sesionProcesadaRepository;
    private final IdempotenciaSesionService idempotenciaSesionService;

    /**
     * Importa una sesion de servidor. Idempotente por contenido: si el mismo JSON
     * ya fue importado para esa carrera, devuelve el tipo sin reprocesar.
     *
     * @param nombreArchivo opcional, solo para trazabilidad. La ingesta por HTTP
     *                     no lo envia.
     */
    @Transactional
    public String importarSesion(Long carreraId, SesionServerData sesion) {
        Carrera carrera = carreraService.getEntity(carreraId);
        return importar(carrera, sesion, null);
    }

    @Transactional
    public String importarSesion(Long carreraId, SesionServerData sesion, String nombreArchivo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        return importar(carrera, sesion, nombreArchivo);
    }

    /**
     * Resultado de una importacion: indica si fue nueva o ya existia.
     */
    public record ResultadoImportacion(String tipo, String clave, boolean yaProcesada) {
    }

    @Transactional
    public ResultadoImportacion importarConIdempotencia(Long carreraId, SesionServerData sesion,
                                                         String nombreArchivo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        String clave = idempotenciaSesionService.claveSesion(carreraId, sesion);

        Optional<SesionProcesada> existente = sesionProcesadaRepository.findByClave(clave);
        if (existente.isPresent()) {
            return new ResultadoImportacion(existente.get().getTipo(), clave, true);
        }

        String tipo = importar(carrera, sesion, nombreArchivo);
        registrarProcesada(carrera, clave, nombreArchivo, tipo);
        return new ResultadoImportacion(tipo, clave, false);
    }

    private String importar(Carrera carrera, SesionServerData sesion) {
        return importar(carrera, sesion, null);
    }

    private String importar(Carrera carrera, SesionServerData sesion, String nombreArchivo) {
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

    @Transactional
    public void registrarProcesada(Long carreraId, String nombreArchivo, String tipo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        String clave = idempotenciaSesionService.claveDesdeNombre(nombreArchivo);
        registrarProcesada(carrera, clave, nombreArchivo, tipo);
    }

    private void registrarProcesada(Carrera carrera, String clave, String nombreArchivo, String tipo) {
        if (sesionProcesadaRepository.existsByClave(clave)) {
            return;
        }
        sesionProcesadaRepository.save(SesionProcesada.builder()
                .carrera(carrera)
                .clave(clave)
                .nombreArchivo(nombreArchivo)
                .tipo(tipo)
                .build());
    }

    public boolean yaProcesada(String nombreArchivo) {
        return sesionProcesadaRepository.existsByNombreArchivo(nombreArchivo);
    }

    public boolean yaProcesadaPorClave(String clave) {
        return sesionProcesadaRepository.existsByClave(clave);
    }

    public Carrera resolverCarrera(SesionServerData sesion, LocalDateTime momentoSesion) {
        return carreraResolverService.resolverCarrera(sesion, momentoSesion);
    }
}