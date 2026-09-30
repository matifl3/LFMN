package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionProcesada;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.SesionProcesadaRepository;
import org.example.lfmnacional.service.sesion.ImportadorSesion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionServidorService {

    private final CarreraService carreraService;
    private final CarreraResolverService carreraResolverService;
    private final List<ImportadorSesion> importadores;
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
        ImportadorSesion importador = importadores.stream()
                .filter(i -> i.tipo().equals(tipo))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tipo de sesion no soportado: " + sesion.type()));
        return importador.importar(carrera, sesion);
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

    public Carrera resolverCarrera(SesionServerData sesion, LocalDateTime momentoSesion) {
        return carreraResolverService.resolverCarrera(sesion, momentoSesion);
    }
}