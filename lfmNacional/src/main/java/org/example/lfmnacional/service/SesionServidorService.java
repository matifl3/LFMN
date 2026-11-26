package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.SesionYaImportadaException;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.example.lfmnacional.service.sesion.DeshacerSesionService;
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
    private final DeshacerSesionService deshacerSesionService;

    /**
     * Resultado de una importacion: indica si fue nueva o ya existia.
     */
    public record ResultadoImportacion(String tipo, String clave, boolean yaProcesada) {
    }

    /**
     * Importa una sesion de servidor. Idempotente por contenido: si el mismo JSON
     * ya fue importado para esa carrera, no reprocesa.
     *
     * <p>Si la carrera ya tiene otra sesion del mismo tipo (otro contenido), no
     * se mezcla con la anterior: se rechaza y hay que usar {@link #reimportar}.
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

        ImportadorSesion importador = importadorPara(sesion);
        if (sesionProcesadaRepository.existsByCarrera_IdAndTipo(carreraId, importador.tipo())) {
            throw new SesionYaImportadaException(importador.tipo(), "La carrera " + carrera.getNombre()
                    + " ya tiene una sesion " + importador.tipo()
                    + " importada con otro contenido. Para reemplazarla hay que reimportarla (ADMIN o COMISARIO).");
        }
        String tipo = importador.importar(carrera, sesion);
        registrarProcesada(carrera, clave, nombreArchivo, tipo);
        return new ResultadoImportacion(tipo, clave, false);
    }

    /**
     * Reemplaza la sesion de ese tipo de la carrera por una corregida: deshace
     * lo que habia dejado la anterior y vuelve a importar. Ver
     * {@link DeshacerSesionService} para que se revierte y que no.
     *
     * <p>Si el contenido es el mismo que el ya importado, no hay nada que
     * corregir y devuelve {@code yaProcesada}, igual que la importacion normal.
     * Si la carrera no tenia esa sesion, es una importacion comun.
     */
    @Transactional
    public ResultadoImportacion reimportar(Long carreraId, SesionServerData sesion, String nombreArchivo) {
        Carrera carrera = carreraService.getEntity(carreraId);
        String clave = idempotenciaSesionService.claveSesion(carreraId, sesion);
        Optional<SesionProcesada> existente = sesionProcesadaRepository.findByClave(clave);
        if (existente.isPresent()) {
            return new ResultadoImportacion(existente.get().getTipo(), clave, true);
        }

        ImportadorSesion importador = importadorPara(sesion);
        String tipo = importador.tipo();
        DeshacerSesionService.EstadoAnterior anterior = null;
        if (TIPO_RACE.equals(tipo)) {
            anterior = deshacerSesionService.deshacerCarrera(carrera);
        } else if (TIPO_QUALIFY.equals(tipo)) {
            deshacerSesionService.deshacerClasificacion(carrera);
        }
        sesionProcesadaRepository.deleteByCarrera_IdAndTipo(carreraId, tipo);

        importador.importar(carrera, sesion);
        registrarProcesada(carrera, clave, nombreArchivo, tipo);

        if (anterior != null) {
            deshacerSesionService.restaurarCampeonato(carrera, anterior);
        } else if (TIPO_QUALIFY.equals(tipo)) {
            deshacerSesionService.recalcularPole(carrera);
        }
        return new ResultadoImportacion(tipo, clave, false);
    }

    private static final String TIPO_RACE = "RACE";
    private static final String TIPO_QUALIFY = "QUALIFY";

    private ImportadorSesion importadorPara(SesionServerData sesion) {
        if (sesion == null) {
            throw new BusinessException("El JSON de sesion es invalido");
        }
        String tipo = sesion.type() != null ? sesion.type().toUpperCase() : "";
        return importadores.stream()
                .filter(i -> i.tipo().equals(tipo))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tipo de sesion no soportado: " + sesion.type()));
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