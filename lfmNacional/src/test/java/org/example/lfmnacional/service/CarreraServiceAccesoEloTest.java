package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.service.rating.EloCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarreraServiceAccesoEloTest {

    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private CampeonatoService campeonatoService;
    @Mock
    private ArchivoCarreraService archivoCarreraService;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private EloCalculator eloCalculator;

    @InjectMocks
    private CarreraService carreraService;

    private Carrera carrera;
    private Usuario piloto;

    @BeforeEach
    void setUp() {
        carrera = Carrera.builder().id(1L).nombre("Race").servidor("srv.lfm.com").contrasenaServidor("s3cr3t").build();
        piloto = Usuario.builder().id(7L).nombrePiloto("Piloto7").elo(1800).safetyRating(150).build();
        lenient().when(carreraRepository.findById(1L)).thenReturn(Optional.of(carrera));
        lenient().when(eloCalculator.calcularCambio(anyInt(), anyInt(), anyInt(), anyList()))
                .thenAnswer(inv -> ((Integer) inv.getArgument(1)) * 10);
    }

    @Test
    void accesoServidorInscriptoDevuelveDatos() {
        Inscripcion inscripto = Inscripcion.builder().carrera(carrera).usuario(piloto).estado(EstadoInscripcion.INSCRIPTO).build();
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 7L)).thenReturn(Optional.of(inscripto));

        var response = carreraService.accesoServidor(1L, 7L);

        assertThat(response.servidor()).isEqualTo("srv.lfm.com");
        assertThat(response.contrasenaServidor()).isEqualTo("s3cr3t");
    }

    @Test
    void accesoServidorSinInscripcionRechaza() {
        when(inscripcionRepository.findByCarrera_IdAndUsuario_Id(1L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carreraService.accesoServidor(1L, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("solo es visible para pilotos inscriptos");
    }

    @Test
    void accesoServidorSinSesionRechaza() {
        assertThatThrownBy(() -> carreraService.accesoServidor(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("iniciar sesion");
    }

    @Test
    void eloEstimadoSinSesionRechaza() {
        assertThatThrownBy(() -> carreraService.eloEstimado(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("iniciar sesion");
    }

    @Test
    void eloEstimadoSinInscriptosRechaza() {
        when(inscripcionRepository.findByCarrera_IdAndEstado(1L, EstadoInscripcion.INSCRIPTO)).thenReturn(List.of());

        assertThatThrownBy(() -> carreraService.eloEstimado(1L, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene pilotos inscriptos");
    }

    @Test
    void eloEstimadoUsuarioNoInscriptoRechaza() {
        Usuario otro = Usuario.builder().id(9L).nombrePiloto("Otro").build();
        Inscripcion ajena = Inscripcion.builder().carrera(carrera).usuario(otro).estado(EstadoInscripcion.INSCRIPTO).build();
        when(inscripcionRepository.findByCarrera_IdAndEstado(1L, EstadoInscripcion.INSCRIPTO)).thenReturn(List.of(ajena));

        assertThatThrownBy(() -> carreraService.eloEstimado(1L, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("estar inscripto");
    }

    @Test
    void eloEstimadoCalculaPosicionYDelta() {
        Usuario rival = Usuario.builder().id(10L).nombrePiloto("Rival").elo(1500).build();
        Inscripcion propia = Inscripcion.builder().carrera(carrera).usuario(piloto).estado(EstadoInscripcion.INSCRIPTO).build();
        Inscripcion otra = Inscripcion.builder().carrera(carrera).usuario(rival).estado(EstadoInscripcion.INSCRIPTO).build();
        when(inscripcionRepository.findByCarrera_IdAndEstado(1L, EstadoInscripcion.INSCRIPTO))
                .thenReturn(List.of(propia, otra));

        var response = carreraService.eloEstimado(1L, 7L);

        assertThat(response.posicionEsperada()).isEqualTo(1);
        assertThat(response.deltaEsperado()).isEqualTo(10);
        assertThat(response.deltaMejorCaso()).isEqualTo(10);
        assertThat(response.deltaPeorCaso()).isEqualTo(20);
        assertThat(response.detalle()).hasSize(2);
    }
}