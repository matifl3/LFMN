package org.example.lfmnacional.service.sesion;

import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.service.IncidenteAutoGenService;
import org.example.lfmnacional.service.ResultadoImportService;
import org.example.lfmnacional.service.VueltaImportService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImportadorRace extends ImportadorSesion {

    private final ResultadoImportService resultadoImportService;
    private final VueltaImportService vueltaImportService;
    private final IncidenteAutoGenService incidenteAutoGenService;

    public ImportadorRace(ResultadoImportService resultadoImportService,
                          VueltaImportService vueltaImportService,
                          IncidenteAutoGenService incidenteAutoGenService) {
        super("RACE");
        this.resultadoImportService = resultadoImportService;
        this.vueltaImportService = vueltaImportService;
        this.incidenteAutoGenService = incidenteAutoGenService;
    }

    @Override
    protected void procesar(Carrera carrera, SesionServerData sesion) {
        resultadoImportService.importarResultados(carrera, sesion);
        try {
            vueltaImportService.importarVueltas(carrera, sesion, tipo());
        } catch (Exception e) {
            log.warn("Error al importar vueltas para carrera {}: {}", carrera.getNombre(), e.getMessage());
        }
        try {
            incidenteAutoGenService.autogenerarIncidentes(carrera, sesion);
        } catch (Exception e) {
            log.warn("Error al autogenerar incidentes para carrera {}: {}", carrera.getNombre(), e.getMessage());
        }
    }
}