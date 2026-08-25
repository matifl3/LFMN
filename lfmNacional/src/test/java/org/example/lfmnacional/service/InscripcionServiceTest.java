package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private CarreraService carreraService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private InscripcionService inscripcionService;

    private Categoria categoria;
    private Campeonato campeonato;
    private Carrera carrera;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("GT3").eloMinimo(1000).eloMaximo(2000).build();
        campeonato = Campeonato.builder().id(1L).nombre("Champ").estado(EstadoCampeonato.ACTIVO).categoria(categoria).build();
        carrera = Carrera.builder().id(1L).nombre("Race").campeonato(campeonato).estado(EstadoCarrera.PROGRAMADA)
                .fecha(LocalDateTime.now().plusHours(2)).cupoMaximo(3).build();
        usuario = Usuario.builder().id(1L).nombrePiloto("Piloto1").elo(1500).safetyRating(100).build();
    }

    @Test
    void inscribirseExitoso() {
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.empty());
        when(inscripcionRepository.countByCarrera_IdAndEstado(1L, EstadoInscripcion.INSCRIPTO)).thenReturn(0L);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = inscripcionService.inscribirse(1L, 1L);

        assertThat(response.estado()).isEqualTo(EstadoInscripcion.INSCRIPTO);
    }

    @Test
    void listaEsperaCuandoLleno() {
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.empty());
        when(inscripcionRepository.countByCarrera_IdAndEstado(1L, EstadoInscripcion.INSCRIPTO)).thenReturn(3L);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = inscripcionService.inscribirse(1L, 1L);

        assertThat(response.estado()).isEqualTo(EstadoInscripcion.LISTA_ESPERA);
    }

    @Test
    void cupoMaximoNullSiempreInscripto() {
        carrera.setCupoMaximo(null);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.empty());
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = inscripcionService.inscribirse(1L, 1L);

        assertThat(response.estado()).isEqualTo(EstadoInscripcion.INSCRIPTO);
    }

    @Test
    void inscripcionDuplicadaLanzaExcepcion() {
        Inscripcion existente = Inscripcion.builder().carrera(carrera).usuario(usuario).estado(EstadoInscripcion.INSCRIPTO).build();
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> inscripcionService.inscribirse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya esta inscripto");
    }

    @Test
    void reactivarDesdeCancelada() {
        Inscripcion cancelada = Inscripcion.builder().id(10L).carrera(carrera).usuario(usuario).estado(EstadoInscripcion.CANCELADA).build();
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(cancelada));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = inscripcionService.inscribirse(1L, 1L);

        assertThat(response.estado()).isEqualTo(EstadoInscripcion.INSCRIPTO);
        verify(inscripcionRepository).save(cancelada);
    }

    @Test
    void carreraNoProgramadaRechaza() {
        carrera.setEstado(EstadoCarrera.EN_CURSO);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);

        assertThatThrownBy(() -> inscripcionService.inscribirse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene inscripciones abiertas");
    }

    @Test
    void carreraCercaEnTiempoRechaza() {
        carrera.setFecha(LocalDateTime.now().plusMinutes(3));
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);

        assertThatThrownBy(() -> inscripcionService.inscribirse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya estan cerradas");
    }

    @Test
    void eloFueraDeRangoRechaza() {
        usuario.setElo(500);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);

        assertThatThrownBy(() -> inscripcionService.inscribirse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("menor al minimo");
    }

    @Test
    void eloMayorAlMaximoRechaza() {
        usuario.setElo(2500);
        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(usuarioService.getEntity(1L)).thenReturn(usuario);

        assertThatThrownBy(() -> inscripcionService.inscribirse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("supera el maximo");
    }

    @Test
    void bajaCancelaInscripcion() {
        Inscripcion inscripcion = Inscripcion.builder().id(1L).carrera(carrera).usuario(usuario).estado(EstadoInscripcion.INSCRIPTO).build();
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = inscripcionService.baja(1L);

        assertThat(response.estado()).isEqualTo(EstadoInscripcion.CANCELADA);
    }

    @Test
    void bajaPromueveListaEspera() {
        Usuario usuario2 = Usuario.builder().id(2L).nombrePiloto("Piloto2").elo(1500).safetyRating(100).build();
        Inscripcion inscripto = Inscripcion.builder().id(1L).carrera(carrera).usuario(usuario).estado(EstadoInscripcion.INSCRIPTO).fechaInscripcion(LocalDateTime.now().minusHours(1)).build();
        Inscripcion espera = Inscripcion.builder().id(2L).carrera(carrera).usuario(usuario2).estado(EstadoInscripcion.LISTA_ESPERA).fechaInscripcion(LocalDateTime.now()).build();

        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripto));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inscripcionRepository.findByCarrera_IdAndEstado(1L, EstadoInscripcion.LISTA_ESPERA))
                .thenReturn(List.of(espera));

        inscripcionService.baja(1L);

        verify(inscripcionRepository).save(argThat(i -> i.getUsuario().getId().equals(2L) && i.getEstado() == EstadoInscripcion.INSCRIPTO));
    }

    @Test
    void bajaYaCanceladaLanzaExcepcion() {
        Inscripcion inscripcion = Inscripcion.builder().id(1L).carrera(carrera).usuario(usuario).estado(EstadoInscripcion.CANCELADA).build();
        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));

        assertThatThrownBy(() -> inscripcionService.baja(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue cancelada");
    }

    @Test
    void cancelarEnVentanaDe5MinutosLanzaExcepcion() {
        carrera.setFecha(LocalDateTime.now().plusMinutes(3));
        when(carreraService.getEntity(1L)).thenReturn(carrera);

        assertThatThrownBy(() -> inscripcionService.cancelar(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya estan cerradas");
    }
}
