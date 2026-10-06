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
    @Mock private CampeonatoService campeonatoService;

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
        Usuario u1 = Usuario.builder().id(1L).nombrePiloto("P1").elo(1500).safetyRating(100).build();
        Usuario u2 = Usuario.builder().id(2L).nombrePiloto("P2").elo(1500).safetyRating(100).build();
        Usuario u3 = Usuario.builder().id(3L).nombrePiloto("P3").elo(1500).safetyRating(100).build();
        Usuario u4 = Usuario.builder().id(4L).nombrePiloto("P4").elo(1500).safetyRating(100).build();
        ResultadoCarrera r1 = ResultadoCarrera.builder().id(1L).carrera(carrera).usuario(u2).posicionFinal(1).build();
        ResultadoCarrera r2 = ResultadoCarrera.builder().id(2L).carrera(carrera).usuario(u1).posicionFinal(2).build();
        ResultadoCarrera r3 = ResultadoCarrera.builder().id(3L).carrera(carrera).usuario(u3).posicionFinal(3).build();
        ResultadoCarrera r4 = ResultadoCarrera.builder().id(4L).carrera(carrera).usuario(u4).posicionFinal(4).build();

        when(usuarioService.getEntity(1L)).thenReturn(u1);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(1L))
                .thenReturn(List.of(r1, r2, r3, r4));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        // El sancionado (u1) es 2do y pierde 2 puestos: cae 4to. Con la cuenta
        // anterior (2 + 2 = 4) empataba con el 4to y el renumerado lo dejaba 3ro,
        // o sea la penalizacion se perdia a medias.
        SancionRequest request = new SancionRequest(1L, 1L, null, TipoSancion.PUESTOS, 2, "Test", OrigenSancion.ADMIN, null, null);
        sancionService.create(request);

        assertThat(r1.getPosicionFinal()).isEqualTo(1);
        assertThat(r2.getPosicionFinal()).isEqualTo(4);
        assertThat(r3.getPosicionFinal()).isEqualTo(2);
        assertThat(r4.getPosicionFinal()).isEqualTo(3);
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

    @Test
    void createPuestosRecalculaLaTablaDelCampeonato() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().id(1L).carrera(carrera).usuario(usuario).posicionFinal(1).build();
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(1L)).thenReturn(List.of(resultado));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        sancionService.create(new SancionRequest(1L, 1L, null, TipoSancion.PUESTOS, 2, "Test", OrigenSancion.ADMIN, null, null));

        // Sin esto, el piloto pierde puestos en la carrera y sigue con los puntos
        // viejo en el campeonato.
        verify(campeonatoService).recalcularPuntos(1L);
    }

    @Test
    void createSegundosRecalculaLaTablaDelCampeonato() {
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

        sancionService.create(new SancionRequest(1L, 1L, null, TipoSancion.SEGUNDOS, 10, "10s", OrigenSancion.ADMIN, null, null));

        verify(campeonatoService).recalcularPuntos(1L);
    }

    @Test
    void createPuestosSinCarreraNoRecalculaLaTabla() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        sancionService.create(new SancionRequest(1L, null, null, TipoSancion.PUESTOS, 2, "Test", OrigenSancion.ADMIN, null, null));

        verify(campeonatoService, never()).recalcularPuntos(any());
    }

    @Test
    void createEloNoRecalculaLaTabla() {
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> {
            Sancion s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        sancionService.create(new SancionRequest(1L, 1L, null, TipoSancion.ELO, -30, "Test", OrigenSancion.ADMIN, null, null));

        verify(campeonatoService, never()).recalcularPuntos(any());
    }

    @Test
    void revertirPuestosRecalculaLaTablaDelCampeonato() {
        // Es el camino de la apelacion aprobada: si no recalcula, el piloto se
        // queda con los puntos del Stewart penalty para siempre.
        ResultadoCarrera resultado = ResultadoCarrera.builder().id(1L).carrera(carrera).usuario(usuario).posicionFinal(3).build();
        when(resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(1L)).thenReturn(List.of(resultado));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));
        Sancion sancion = Sancion.builder().id(9L).usuario(usuario).carrera(carrera)
                .tipo(TipoSancion.PUESTOS).valor(2).origen(OrigenSancion.COMISARIO)
                .efectosAplicados(true).build();

        sancionService.revertirEfectos(sancion);

        // Revertir lo devuelve 2 lugares hacia adelante; como es el unico
        // clasificado queda primero. Lo que importa es que la tabla del
        // campeonato se rehaga.
        assertThat(resultado.getPosicionFinal()).isEqualTo(1);
        verify(campeonatoService).recalcularPuntos(1L);
    }

    @Test
    void revertirPuestosSinCarreraNoRecalculaLaTabla() {
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));
        Sancion sancion = Sancion.builder().id(9L).usuario(usuario).carrera(null)
                .tipo(TipoSancion.PUESTOS).valor(2).efectosAplicados(true).build();

        sancionService.revertirEfectos(sancion);

        verify(campeonatoService, never()).recalcularPuntos(any());
    }
}
