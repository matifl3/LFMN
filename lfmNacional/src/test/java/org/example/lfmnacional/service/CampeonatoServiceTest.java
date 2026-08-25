package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampeonatoServiceTest {

    @Mock
    private CampeonatoRepository campeonatoRepository;
    @Mock
    private CampeonatoPosicionRepository campeonatoPosicionRepository;
    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CampeonatoService campeonatoService;

    private Categoria categoria;
    private Campeonato campeonato;
    private Usuario usuario1;
    private Usuario usuario2;
    private Carrera carrera;

    private static List<CampeonatoPosicion> iterableToList(Iterable<CampeonatoPosicion> iterable) {
        return new ArrayList<>(StreamSupport.stream(iterable.spliterator(), false).toList());
    }

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("GT3").eloMinimo(1000).eloMaximo(2000).build();
        campeonato = Campeonato.builder().id(1L).nombre("Test Championship").temporada("2026").categoria(categoria).estado(EstadoCampeonato.ACTIVO).build();
        usuario1 = Usuario.builder().id(1L).nombrePiloto("Piloto1").elo(1500).safetyRating(100).build();
        usuario2 = Usuario.builder().id(2L).nombrePiloto("Piloto2").elo(1400).safetyRating(90).build();
        carrera = Carrera.builder().id(1L).nombre("Race 1").campeonato(campeonato).estado(EstadoCarrera.FINALIZADA).build();
    }

    @Test
    void puntosPorPosicionP1() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(1).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.empty());
        when(campeonatoPosicionRepository.countByCampeonato_Id(1L)).thenReturn(0L);
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of());

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> {
            List<CampeonatoPosicion> items = iterableToList(iterable);
            return !items.isEmpty() && items.get(0).getPuntos() == 25 && items.get(0).getPosicion() == 1;
        }));
    }

    @Test
    void puntosPorPosicionP2() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(2).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.empty());
        when(campeonatoPosicionRepository.countByCampeonato_Id(1L)).thenReturn(0L);
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of());

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> {
            List<CampeonatoPosicion> items = iterableToList(iterable);
            return !items.isEmpty() && items.get(0).getPuntos() == 18;
        }));
    }

    @Test
    void puntosPorPosicionP11GuardaCeroPuntos() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(11).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of());

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> {
            List<CampeonatoPosicion> items = iterableToList(iterable);
            return !items.isEmpty() && items.get(0).getPuntos() == 0;
        }));
    }

    @Test
    void puntosPorPosicionP0GuardaCeroPuntos() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(0).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of());

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> {
            List<CampeonatoPosicion> items = iterableToList(iterable);
            return !items.isEmpty() && items.get(0).getPuntos() == 0;
        }));
    }

    @Test
    void posicionFinalNullSeIgnora() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(null).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of());

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> iterableToList(iterable).isEmpty()));
    }

    @Test
    void noActivoNoGuardaNada() {
        campeonato.setEstado(EstadoCampeonato.CERRADO);
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(1).carrera(carrera).build();

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, never()).saveAll(any());
    }

    @Test
    void acumulaPuntosEntreCarreras() {
        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(1).carrera(carrera).build();
        CampeonatoPosicion existente = CampeonatoPosicion.builder().campeonato(campeonato).usuario(usuario1).puntos(25).posicion(1).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(existente));
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of(existente));

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeastOnce()).saveAll(argThat(iterable -> {
            List<CampeonatoPosicion> items = iterableToList(iterable);
            return !items.isEmpty() && items.get(0).getPuntos() == 50;
        }));
    }

    @Test
    void recalcularPosicionesOrdenaPorPuntosDesc() {
        CampeonatoPosicion p1 = CampeonatoPosicion.builder().campeonato(campeonato).usuario(usuario1).puntos(50).posicion(1).build();
        CampeonatoPosicion p2 = CampeonatoPosicion.builder().campeonato(campeonato).usuario(usuario2).puntos(30).posicion(2).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(1L)).thenReturn(List.of(p1, p2));

        ResultadoCarrera resultado = ResultadoCarrera.builder().usuario(usuario1).posicionFinal(1).carrera(carrera).build();
        when(campeonatoPosicionRepository.findByCampeonato_IdAndUsuario_Id(1L, 1L)).thenReturn(Optional.of(p1));

        campeonatoService.actualizarPuntos(carrera, List.of(resultado));

        verify(campeonatoPosicionRepository, atLeast(2)).saveAll(any());
    }

    @Test
    void deleteConPosicionesLanzaExcepcion() {
        when(campeonatoRepository.findById(1L)).thenReturn(Optional.of(campeonato));
        when(campeonatoPosicionRepository.existsByCampeonato_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> campeonatoService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene posiciones");
    }
}
