package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.entity.*;
import org.example.lfmnacional.enums.*;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SancionServiceTest {

    @Mock private SancionRepository sancionRepository;
    @Mock private EloSancionRepository eloSancionRepository;
    @Mock private SafetyRatingSancionRepository safetyRatingSancionRepository;
    @Mock private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock private NotificacionRepository notificacionRepository;
    @Mock private ApelacionRepository apelacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private CarreraService carreraService;

    @InjectMocks
    private SancionService sancionService;

    private Categoria categoria;
    private Campeonato campeonato;
    private Carrera carrera;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("GT3").build();
        campeonato = Campeonato.builder().id(1L).nombre("Champ").estado(EstadoCampeonato.ACTIVO).categoria(categoria).build();
        carrera = Carrera.builder().id(1L).nombre("Race").campeonato(campeonato).estado(EstadoCarrera.FINALIZADA).build();
        usuario = Usuario.builder().id(1L).nombrePiloto("Piloto1").elo(1500).safetyRating(100).build();
    }

    @Test
    void createEloAumentaElo() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.ELO, 50, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(usuario.getElo()).isEqualTo(1550);
        verify(usuarioRepository).save(usuario);
        verify(eloSancionRepository).save(any(EloSancion.class));
    }

    @Test
    void createEloDisminuyeElo() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.ELO, -30, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(usuario.getElo()).isEqualTo(1470);
    }

    @Test
    void createSafetyRatingAumentaSR() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.SAFETY_RATING, 10, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(usuario.getSafetyRating()).isEqualTo(110);
        verify(safetyRatingSancionRepository).save(any(SafetyRatingSancion.class));
    }

    @Test
    void createNotifica() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.ELO, 50, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        verify(notificacionRepository).save(argThat(n -> n.getTipo() == TipoNotificacion.PENALIZACION));
    }

    @Test
    void updateConCambioTipoRevierteYAplica() {
        Sancion sancion = Sancion.builder().id(1L).usuario(usuario).carrera(carrera).tipo(TipoSancion.ELO).valor(50)
                .motivo("Old").origen(OrigenSancion.ADMIN).efectosAplicados(true).fecha(LocalDateTime.now()).build();
        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.SAFETY_RATING, 20, "New", OrigenSancion.ADMIN, null, null);
        sancionService.update(1L, request);

        assertThat(usuario.getElo()).isEqualTo(1450);
        assertThat(usuario.getSafetyRating()).isEqualTo(120);
    }

    @Test
    void updateMismaTipoSinCambioNoRevierte() {
        Sancion sancion = Sancion.builder().id(1L).usuario(usuario).carrera(carrera).tipo(TipoSancion.ELO).valor(50)
                .motivo("Old").origen(OrigenSancion.ADMIN).efectosAplicados(true).fecha(LocalDateTime.now()).build();
        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.ELO, 50, "Same", OrigenSancion.ADMIN, null, null);
        sancionService.update(1L, request);

        assertThat(usuario.getElo()).isEqualTo(1500);
        verify(eloSancionRepository, never()).save(any());
    }

    @Test
    void deleteConApelacionesLanzaExcepcion() {
        Sancion sancion = Sancion.builder().id(1L).usuario(usuario).carrera(carrera).tipo(TipoSancion.ELO).valor(50)
                .efectosAplicados(true).fecha(LocalDateTime.now()).build();
        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(apelacionRepository.existsBySancion_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> sancionService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("apelaciones");
    }

    @Test
    void deleteConResolucionLanzaExcepcion() {
        ResolucionIncidente resolucion = ResolucionIncidente.builder().id(1L).build();
        Sancion sancion = Sancion.builder().id(1L).usuario(usuario).carrera(carrera).tipo(TipoSancion.ELO).valor(50)
                .resolucion(resolucion).efectosAplicados(true).fecha(LocalDateTime.now()).build();
        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(apelacionRepository.existsBySancion_Id(1L)).thenReturn(false);

        assertThatThrownBy(() -> sancionService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("resolucion");
    }

    @Test
    void deleteRevierteEfectos() {
        Sancion sancion = Sancion.builder().id(1L).usuario(usuario).carrera(carrera).tipo(TipoSancion.ELO).valor(50)
                .motivo("Test").efectosAplicados(true).fecha(LocalDateTime.now()).build();
        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(apelacionRepository.existsBySancion_Id(1L)).thenReturn(false);

        sancionService.delete(1L);

        assertThat(usuario.getElo()).isEqualTo(1450);
        verify(eloSancionRepository).save(argThat(e -> e.getCambio() == -50));
    }

    @Test
    void createPuestosSinCarreraNoMueve() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, null, null, TipoSancion.PUESTOS, 2, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        verify(resultadoCarreraRepository, never()).findByCarrera_IdAndUsuario_Id(any(), any());
    }

    @Test
    void createPuestosConResultadoMuevePosicion() {
        ResultadoCarrera resultado1 = ResultadoCarrera.builder().id(1L).carrera(carrera).usuario(usuario).posicionFinal(3).tiempoTotal(100000L).build();
        Usuario usuario2 = Usuario.builder().id(2L).nombrePiloto("P2").elo(1500).safetyRating(100).build();
        ResultadoCarrera resultado2 = ResultadoCarrera.builder().id(2L).carrera(carrera).usuario(usuario2).posicionFinal(1).tiempoTotal(90000L).build();
        ResultadoCarrera resultado3 = ResultadoCarrera.builder().id(3L).carrera(carrera).usuario(Usuario.builder().id(3L).nombrePiloto("P3").elo(1500).safetyRating(100).build()).posicionFinal(2).tiempoTotal(95000L).build();

        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(resultadoCarreraRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(resultado1));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.PUESTOS, 2, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(resultado1.getPosicionFinal()).isEqualTo(5);
    }

    @Test
    void createSegundosAgregaTiempo() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().id(1L).carrera(carrera).usuario(usuario).posicionFinal(1).tiempoTotal(100000L).build();
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(resultadoCarreraRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(resultado));
        when(resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(1L)).thenReturn(List.of(resultado));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.SEGUNDOS, 10, "10 segundos", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(resultado.getTiempoTotal()).isEqualTo(110000L);
    }
}
