package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.service.ClasificacionImportService;
import org.springframework.stereotype.Component;

@Component
public class ImportadorQualify extends ImportadorSesion {

    private final ClasificacionImportService clasificacionImportService;

    public ImportadorQualify(ClasificacionImportService clasificacionImportService) {
        super("QUALIFY");
        this.clasificacionImportService = clasificacionImportService;
    }

    @Override
    protected void procesar(Carrera carrera, SesionServerData sesion) {
        clasificacionImportService.importarClasificacion(carrera, sesion);
    }
}