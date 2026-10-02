package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.dto.sesion.EventoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.service.sesion.IdempotenciaSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidenteAutoGenService {

    private final IncidenteRepository incidenteRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraResolverService carreraResolverService;
    private final IdempotenciaSesionService idempotenciaSesionService;

    @Transactional
    public void autogenerarIncidentes(Carrera carrera, SesionServerData sesion) {
        if (sesion.events() == null) {
            return;
        }
        for (EventoSesionData evento : sesion.events()) {
            String guid = evento.driver() != null ? evento.driver().guid() : null;
            if (!carreraResolverService.guidValido(guid)) {
                continue;
            }
            Usuario piloto = usuarioRepository.findByGuidSteam(guid).orElse(null);
            if (piloto == null) {
                continue;
            }

            // Reintentos de ingesta (HTTP con backoff, o watcher reprocesando)
            // no deben duplicar incidentes: se identifica el evento concreto.
            String claveOrigen = idempotenciaSesionService.claveIncidente(carrera.getId(), evento, guid);
            if (incidenteRepository.existsByClaveOrigen(claveOrigen)) {
                log.debug("Incidente ya generado para el evento {} de la carrera {}",
                        evento.type(), carrera.getId());
                continue;
            }

            String descripcion = "Colision detectada automaticamente (" + evento.type() + ")"
                    + (evento.impactSpeed() != null ? " - Impacto: " + Math.round(evento.impactSpeed()) + " km/h" : "");
            Incidente incidente = incidenteRepository.save(Incidente.builder()
                    .carrera(carrera)
                    .reportante(piloto)
                    .descripcion(descripcion)
                    .claveOrigen(claveOrigen)
                    .estado(EstadoIncidente.PENDIENTE)
                    .build());
            incidentePilotoRepository.save(IncidentePiloto.builder()
                    .incidente(incidente)
                    .usuario(piloto)
                    .rol(RolPilotoIncidente.AFECTADO)
                    .build());
        }
    }
}