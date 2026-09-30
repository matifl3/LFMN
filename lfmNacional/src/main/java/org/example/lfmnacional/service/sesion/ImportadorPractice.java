package org.example.lfmnacional.service.sesion;

import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImportadorPractice extends ImportadorSesion {

    public ImportadorPractice() {
        super("PRACTICE");
    }

    @Override
    protected void procesar(Carrera carrera, SesionServerData sesion) {
        log.info("Sesion PRACTICE ignorada (no importa datos)");
    }
}