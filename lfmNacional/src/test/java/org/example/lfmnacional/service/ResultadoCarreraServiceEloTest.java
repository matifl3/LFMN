package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraRequest;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.service.rating.EloCalculator;
import org.example.lfmnacional.service.rating.SrCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Razon de existir: los resultados sin posicion (carga manual) se salteaban al
 * calcular el Elo, pero igual contaban como participantes y su Elo entraba como
 * rival, asi que el cambio de los demas salia desviado.
 */
@ExtendWith(MockitoExtension.class)
class ResultadoCarreraServiceEloTest {

    @Mock
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock
    private EloSancionRepository eloSancionRepository;
    @Mock
    private SafetyRatingSancionRepository safetyRatingSancionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CarreraService carreraService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private CampeonatoService campeonatoService;
    @Mock
    private LogroService logroService;
    @Mock
    private EloCalculator eloCalculator;
    @Mock
    private SrCalculator srCalculator;
    @Mock
    private CampeonatoAccesoService championshipAcceso;

    @InjectMocks
    private ResultadoCarreraService resultadoCarreraService;

    @Test
    void losResultadosSinPosicionNoCuentanComoParticipantesNiComoRivales() {
        Carrera carrera = Carrera.builder().id(1L).nombre("R1").estado(EstadoCarrera.FINALIZADA)
                .campeonato(Campeonato.builder().id(1L).build()).build();
        Usuario ana = Usuario.builder().id(1L).elo(1500).safetyRating(1000).build();
        Usuario beto = Usuario.builder().id(2L).elo(1400).safetyRating(1000).build();
        // Sin posicion y con un Elo bien distinto: si entrara como rival se notaria.
        Usuario caro = Usuario.builder().id(3L).elo(3000).safetyRating(1000).build();

        when(carreraService.getEntity(1L)).thenReturn(carrera);
        when(resultadoCarreraRepository.existsByCarrera_Id(1L)).thenReturn(false);
        when(resultadoCarreraRepository.findByCarrera_IdAndUsuario_Id(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(usuarioService.getEntity(1L)).thenReturn(ana);
        when(usuarioService.getEntity(2L)).thenReturn(beto);
        when(usuarioService.getEntity(3L)).thenReturn(caro);
        when(eloCalculator.calcularCambio(anyInt(), anyInt(), anyInt(), any())).thenReturn(0);
        when(srCalculator.calcularCambio(anyBoolean(), anyInt())).thenReturn(0);

        resultadoCarreraService.cargarResultados(new CargarResultadosRequest(1L, List.of(
                item(1L, 1), item(2L, 2), item(3L, null))));

        // Dos participantes, y cada uno con un solo rival: el otro clasificado.
        verify(eloCalculator).calcularCambio(eq(1500), eq(1), eq(2), argThat(r -> r.equals(List.of(1400))));
        verify(eloCalculator).calcularCambio(eq(1400), eq(2), eq(2), argThat(r -> r.equals(List.of(1500))));
        verify(eloCalculator, never()).calcularCambio(eq(3000), anyInt(), anyInt(), any());
    }

    private static ResultadoCarreraRequest item(Long usuarioId, Integer posicion) {
        return new ResultadoCarreraRequest(1L, usuarioId, posicion, null, null, false, true,
                null, null, null, null, null);
    }
}
