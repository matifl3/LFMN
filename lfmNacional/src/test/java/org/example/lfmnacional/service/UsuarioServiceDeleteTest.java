package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Usuario;
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
import org.example.lfmnacional.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceDeleteTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EloSancionRepository eloSancionRepository;
    @Mock
    private SafetyRatingSancionRepository safetyRatingSancionRepository;
    @Mock
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock
    private ApelacionRepository apelacionRepository;
    @Mock
    private CampeonatoPosicionRepository campeonatoPosicionRepository;
    @Mock
    private CampeonatoMiembroRepository campeonatoMiembroRepository;
    @Mock
    private CampeonatoRepository campeonatoRepository;
    @Mock
    private IncidentePilotoRepository incidentePilotoRepository;
    @Mock
    private IncidenteRepository incidenteRepository;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private ResolucionIncidenteRepository resolucionIncidenteRepository;
    @Mock
    private SancionRepository sancionRepository;
    @Mock
    private SesionClasificacionRepository sesionClasificacionRepository;
    @Mock
    private SetupCalificacionRepository setupCalificacionRepository;
    @Mock
    private SetupComentarioRepository setupComentarioRepository;
    @Mock
    private SetupRepository setupRepository;
    @Mock
    private UsuarioLogroRepository usuarioLogroRepository;
    @Mock
    private UsuarioRecompensaRepository usuarioRecompensaRepository;
    @Mock
    private VotoComisarioRepository votoComisarioRepository;
    @Mock
    private VueltaRepository vueltaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deleteLimpiaDependenciasAntesDeEliminarElUsuario() {
        long id = 5L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(Usuario.builder().id(id).build()));

        usuarioService.delete(id);

        verify(votoComisarioRepository).deleteByComisario_Id(id);
        verify(votoComisarioRepository).deleteByIncidenteReportanteId(id);
        verify(resolucionIncidenteRepository).deleteByComisario_Id(id);
        verify(resolucionIncidenteRepository).deleteByIncidenteReportanteId(id);
        verify(apelacionRepository).deleteByUsuario_Id(id);
        verify(apelacionRepository).deleteBySancionUsuarioId(id);
        verify(apelacionRepository).deleteBySancionResolucionIncidenteReportanteId(id);
        verify(incidentePilotoRepository).deleteByUsuario_Id(id);
        verify(incidentePilotoRepository).deleteByIncidenteReportanteId(id);
        verify(setupComentarioRepository).deleteByUsuario_Id(id);
        verify(setupComentarioRepository).deleteBySetupAutorId(id);
        verify(setupCalificacionRepository).deleteByUsuario_Id(id);
        verify(setupCalificacionRepository).deleteBySetupAutorId(id);
        verify(sancionRepository).deleteByResolucionIncidenteReportanteId(id);
        verify(sancionRepository).deleteByUsuario_Id(id);
        verify(incidenteRepository).deleteByReportante_Id(id);
        verify(setupRepository).deleteByAutor_Id(id);
        verify(inscripcionRepository).deleteByUsuario_Id(id);
        verify(notificacionRepository).deleteByUsuario_Id(id);
        verify(resultadoCarreraRepository).deleteByUsuario_Id(id);
        verify(vueltaRepository).deleteByUsuario_Id(id);
        verify(sesionClasificacionRepository).deleteByUsuario_Id(id);
        verify(campeonatoMiembroRepository).deleteByUsuario_Id(id);
        verify(campeonatoPosicionRepository).deleteByUsuario_Id(id);
        verify(eloSancionRepository).deleteByUsuario_Id(id);
        verify(safetyRatingSancionRepository).deleteByUsuario_Id(id);
        verify(usuarioLogroRepository).deleteByUsuario_Id(id);
        verify(usuarioRecompensaRepository).deleteByUsuario_Id(id);
        // Desvincular antes de borrar: la FK de campeonato.admin_id no puede quedar colgando.
        verify(campeonatoRepository).desvincularAdmin(id);
        verify(usuarioRepository).deleteById(id);
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    void deleteRespetaOrdenFkDeLosAgregados() {
        long id = 7L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(Usuario.builder().id(id).build()));

        usuarioService.delete(id);

        InOrder orden = org.mockito.Mockito.inOrder(
                apelacionRepository, sancionRepository, resolucionIncidenteRepository,
                setupComentarioRepository, setupCalificacionRepository, setupRepository,
                incidentePilotoRepository, incidenteRepository, votoComisarioRepository, usuarioRepository);
        orden.verify(apelacionRepository).deleteBySancionUsuarioId(id);
        orden.verify(apelacionRepository).deleteBySancionResolucionIncidenteReportanteId(id);
        orden.verify(sancionRepository).deleteByResolucionIncidenteReportanteId(id);
        orden.verify(votoComisarioRepository).deleteByIncidenteReportanteId(id);
        orden.verify(resolucionIncidenteRepository).deleteByIncidenteReportanteId(id);
        orden.verify(incidentePilotoRepository).deleteByIncidenteReportanteId(id);
        orden.verify(setupComentarioRepository).deleteBySetupAutorId(id);
        orden.verify(setupCalificacionRepository).deleteBySetupAutorId(id);
        orden.verify(sancionRepository).deleteByUsuario_Id(id);
        orden.verify(incidenteRepository).deleteByReportante_Id(id);
        orden.verify(setupRepository).deleteByAutor_Id(id);
        orden.verify(usuarioRepository).deleteById(id);
    }

    @Test
    void deleteUsuarioInexistenteNoBorraNada() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).deleteById(any());
    }
}