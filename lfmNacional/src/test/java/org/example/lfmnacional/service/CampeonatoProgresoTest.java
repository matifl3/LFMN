package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.campeonato.ProgresoCampeonatoResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Progreso de temporada que muestra el hero del home ("Ronda X de N"). */
@ExtendWith(MockitoExtension.class)
class CampeonatoProgresoTest {

    @Mock
    private CampeonatoRepository campeonatoRepository;
    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private CampeonatoAccesoService accesoService;

    @InjectMocks
    private CampeonatoService campeonatoService;

    private final Campeonato campeonato = Campeonato.builder().id(5L).nombre("GT3 2026")
            .estado(EstadoCampeonato.ACTIVO).visibilidad(VisibilidadCampeonato.PUBLICO).build();

    @Test
    void cuentaCorridasYTotalSinCanceladas() {
        when(campeonatoRepository.findById(5L)).thenReturn(Optional.of(campeonato));
        when(carreraRepository.countByCampeonato_IdAndEstadoNot(5L, EstadoCarrera.CANCELADA)).thenReturn(16L);
        when(carreraRepository.countByCampeonato_IdAndEstado(5L, EstadoCarrera.FINALIZADA)).thenReturn(7L);

        ProgresoCampeonatoResponse progreso = campeonatoService.progreso(5L, null);

        assertThat(progreso.rondasTotales()).isEqualTo(16L);
        assertThat(progreso.rondasCorridas()).isEqualTo(7L);
    }

    @Test
    void sinAccesoAlContenidoNoCuentaNada() {
        when(campeonatoRepository.findById(5L)).thenReturn(Optional.of(campeonato));
        doThrow(new BusinessException("privado")).when(accesoService).exigirVeContenido(any(), any());

        assertThatThrownBy(() -> campeonatoService.progreso(5L, null)).isInstanceOf(BusinessException.class);
        verify(carreraRepository, never()).countByCampeonato_IdAndEstado(anyLong(), any());
    }
}
