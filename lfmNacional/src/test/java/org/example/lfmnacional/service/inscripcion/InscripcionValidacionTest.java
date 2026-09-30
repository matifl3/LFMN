package org.example.lfmnacional.service.inscripcion;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscripcionValidacionTest {

    @Mock
    private CampeonatoAccesoService accesoService;

    private Categoria categoria;
    private Campeonato campeonato;
    private Carrera carrera;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("GT3").eloMinimo(1000).eloMaximo(2000).build();
        campeonato = Campeonato.builder().id(1L).categoria(categoria)
                .visibilidad(VisibilidadCampeonato.PUBLICO).build();
        carrera = Carrera.builder().id(1L).nombre("Race").campeonato(campeonato)
                .estado(EstadoCarrera.PROGRAMADA).fecha(LocalDateTime.now().plusHours(2)).build();
        usuario = Usuario.builder().id(1L).nombrePiloto("Piloto1").elo(1500).build();
    }

    private ValidadorInscripcion cadena() {
        ValidadorInscripcion abiertas = new InscripcionesAbiertasValidador();
        ValidadorInscripcion pertenencia = new PertenenciaValidador(accesoService);
        abiertas.setSiguiente(pertenencia);
        pertenencia.setSiguiente(new RequisitosEloValidador());
        return abiertas;
    }

    @Test
    void aceptaCuandoCumpleTodosLosEslabones() {
        lenient().when(accesoService.puedeParticiparEnCarrera(usuario, carrera)).thenReturn(true);
        cadena().validar(carrera, usuario);
    }

    @Test
    void eloFueraDeRangoEsElUltimoEslabon() {
        lenient().when(accesoService.puedeParticiparEnCarrera(usuario, carrera)).thenReturn(true);
        usuario.setElo(500);

        assertThatThrownBy(() -> cadena().validar(carrera, usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("menor al minimo");
    }

    @Test
    void faltaDePertenenciaFrenaAntesDelElo() {
        when(accesoService.puedeParticiparEnCarrera(usuario, carrera)).thenReturn(false);
        usuario.setElo(500);

        assertThatThrownBy(() -> cadena().validar(carrera, usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("campeonato privado");
        verify(accesoService).puedeParticiparEnCarrera(usuario, carrera);
    }

    @Test
    void campeonatoPrivadoSalteaElFiltroDeElo() {
        campeonato.setVisibilidad(VisibilidadCampeonato.PRIVADO);
        when(accesoService.puedeParticiparEnCarrera(usuario, carrera)).thenReturn(true);
        usuario.setElo(500);

        cadena().validar(carrera, usuario);
    }

    @Test
    void carreraCerradaFrenaAntesDePertenencia() {
        carrera.setEstado(EstadoCarrera.EN_CURSO);

        assertThatThrownBy(() -> cadena().validar(carrera, usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene inscripciones abiertas");
        verifyNoInteractions(accesoService);
    }

    @Test
    void ensambladorEncadenaLosValidadoresRegistrados() {
        InscripcionValidacion validacion = new InscripcionValidacion(List.of(
                new InscripcionesAbiertasValidador(),
                new PertenenciaValidador(accesoService),
                new RequisitosEloValidador()));
        carrera.setEstado(EstadoCarrera.EN_CURSO);

        assertThatThrownBy(() -> validacion.validar(carrera, usuario))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene inscripciones abiertas");
        verifyNoInteractions(accesoService);
    }
}