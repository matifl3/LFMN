package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private CampeonatoMiembroRepository miembroRepository;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private CampeonatoAccesoService accesoService;

    @InjectMocks
    private CampeonatoService campeonatoService;

    private Categoria categoria;
    private Campeonato campeonato;
    private Usuario usuario1;
    private Usuario usuario2;
    private Carrera carrera;
    private Usuario admin;

    private static List<CampeonatoPosicion> iterableToList(Iterable<CampeonatoPosicion> iterable) {
        return new ArrayList<>(StreamSupport.stream(iterable.spliterator(), false).toList());
    }

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nombre("GT3").eloMinimo(1000).eloMaximo(2000).build();
        campeonato = Campeonato.builder().id(1L).nombre("Test Championship").temporada("2026").categoria(categoria).estado(EstadoCampeonato.ACTIVO).visibilidad(VisibilidadCampeonato.PUBLICO).build();
        usuario1 = Usuario.builder().id(1L).nombrePiloto("Piloto1").elo(1500).safetyRating(100).build();
        usuario2 = Usuario.builder().id(2L).nombrePiloto("Piloto2").elo(1400).safetyRating(90).build();
        admin = Usuario.builder().id(9L).nombrePiloto("Admin").rol(Rol.ADMIN).build();
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

        assertThatThrownBy(() -> campeonatoService.delete(1L, admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene posiciones");
    }

    @Test
    void deleteLimpiaLosMiembrosDelCampeonato() {
        when(campeonatoRepository.findById(1L)).thenReturn(Optional.of(campeonato));
        when(campeonatoPosicionRepository.existsByCampeonato_Id(1L)).thenReturn(false);

        campeonatoService.delete(1L, admin);

        verify(miembroRepository).deleteByCampeonato_Id(1L);
        verify(campeonatoRepository).delete(campeonato);
    }

    @Test
    void adminCampeonatoCreaSiempreEnPrivato() {
        Usuario dueno = Usuario.builder().id(7L).nombrePiloto("Dueno").rol(Rol.ADMIN_CAMPEONATO).build();
        when(accesoService.puedeCrearCampeonato(dueno)).thenReturn(true);
        when(accesoService.esAdminGlobal(dueno)).thenReturn(false);
        when(categoriaService.getEntity(1L)).thenReturn(categoria);
        when(campeonatoRepository.save(any(Campeonato.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        campeonatoService.create(
                new org.example.lfmnacional.dto.campeonato.CampeonatoRequest(
                        "Copa Nocturna", "2026", 1L, null, null, VisibilidadCampeonato.PUBLICO),
                dueno);

        ArgumentCaptor<Campeonato> captor = ArgumentCaptor.forClass(Campeonato.class);
        verify(campeonatoRepository).save(captor.capture());
        assertThat(captor.getValue().getVisibilidad()).isEqualTo(VisibilidadCampeonato.PRIVADO);
        assertThat(captor.getValue().getAdmin()).isSameAs(dueno);
    }

    @Test
    void adminGlobalPuedeElegirLaVisibilidad() {
        when(accesoService.puedeCrearCampeonato(admin)).thenReturn(true);
        when(accesoService.esAdminGlobal(admin)).thenReturn(true);
        when(categoriaService.getEntity(1L)).thenReturn(categoria);
        when(campeonatoRepository.save(any(Campeonato.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        campeonatoService.create(
                new org.example.lfmnacional.dto.campeonato.CampeonatoRequest(
                        "Liga Abierta", "2026", 1L, null, null, VisibilidadCampeonato.PUBLICO),
                admin);

        ArgumentCaptor<Campeonato> captor = ArgumentCaptor.forClass(Campeonato.class);
        verify(campeonatoRepository).save(captor.capture());
        assertThat(captor.getValue().getVisibilidad()).isEqualTo(VisibilidadCampeonato.PUBLICO);
        assertThat(captor.getValue().getAdmin()).isNull();
    }
}
