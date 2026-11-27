package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.ResolucionIncidenteRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.example.lfmnacional.repository.UsuarioLogroRepository;
import org.example.lfmnacional.repository.UsuarioRecompensaRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Borrar un usuario con todo lo que cuelga de el, en el orden que piden las FK
 * (GRASP Fabricacion pura). Necesita tocar casi todas las tablas, y por eso vive
 * aparte: antes esas ~22 dependencias estaban en {@code UsuarioService} y lo
 * acoplaban a todo el modelo aunque el resto del servicio no las usara.
 */
@Service
@RequiredArgsConstructor
public class BajaUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CampeonatoMiembroRepository campeonatoMiembroRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final ApelacionRepository apelacionRepository;
    private final CampeonatoPosicionRepository campeonatoPosicionRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final IncidenteRepository incidenteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;
    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final SancionRepository sancionRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final SetupCalificacionRepository setupCalificacionRepository;
    private final SetupComentarioRepository setupComentarioRepository;
    private final SetupRepository setupRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;
    private final UsuarioRecompensaRepository usuarioRecompensaRepository;
    private final VotoComisarioRepository votoComisarioRepository;
    private final VueltaRepository vueltaRepository;
    private final CampeonatoAccesoService accesoService;

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id, Usuario actor) {
        if (accesoService.esPropio(actor, id)) {
            throw new BusinessException("No podes eliminar tu propia cuenta");
        }
        usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
        // Referencias directas hacia el usuario
        votoComisarioRepository.deleteByComisario_Id(id);
        resolucionIncidenteRepository.deleteByComisario_Id(id);
        apelacionRepository.deleteByUsuario_Id(id);
        incidentePilotoRepository.deleteByUsuario_Id(id);
        setupComentarioRepository.deleteByUsuario_Id(id);
        setupCalificacionRepository.deleteByUsuario_Id(id);
        // Dependencias de los agregados del usuario (antes de borrarlos)
        apelacionRepository.deleteBySancionUsuarioId(id);
        apelacionRepository.deleteBySancionResolucionIncidenteReportanteId(id);
        sancionRepository.deleteByResolucionIncidenteReportanteId(id);
        votoComisarioRepository.deleteByIncidenteReportanteId(id);
        resolucionIncidenteRepository.deleteByIncidenteReportanteId(id);
        incidentePilotoRepository.deleteByIncidenteReportanteId(id);
        setupComentarioRepository.deleteBySetupAutorId(id);
        setupCalificacionRepository.deleteBySetupAutorId(id);
        // Agregados del usuario
        sancionRepository.deleteByUsuario_Id(id);
        incidenteRepository.deleteByReportante_Id(id);
        setupRepository.deleteByAutor_Id(id);
        // Filas propias sin dependencias
        inscripcionRepository.deleteByUsuario_Id(id);
        notificacionRepository.deleteByUsuario_Id(id);
        resultadoCarreraRepository.deleteByUsuario_Id(id);
        vueltaRepository.deleteByUsuario_Id(id);
        sesionClasificacionRepository.deleteByUsuario_Id(id);
        campeonatoMiembroRepository.deleteByUsuario_Id(id);
        campeonatoPosicionRepository.deleteByUsuario_Id(id);
        eloSancionRepository.deleteByUsuario_Id(id);
        safetyRatingSancionRepository.deleteByUsuario_Id(id);
        usuarioLogroRepository.deleteByUsuario_Id(id);
        usuarioRecompensaRepository.deleteByUsuario_Id(id);
        // Si era dueno de campeonato privado lo deja sin dueno en vez de borrarle
        // los campeonato: la FK de campeonato.admin_id no puede quedar colgando.
        campeonatoRepository.desvincularAdmin(id);
        usuarioRepository.deleteById(id);
    }
}
