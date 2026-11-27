package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.carrera.CarreraRequest;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.service.rating.EloCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Una carrera con resultados ya movio Elo/SR de los pilotos y sumo puntos a la
 * tabla de su campeonato. Borrarla o moverla de campeonato dejaria esos valores
 * sin origen, asi que el servicio lo rechaza.
 */
@ExtendWith(MockitoExtension.class)
class CarreraServiceIntegridadTest {

    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private CampeonatoService campeonatoService;
    @Mock
    private CampeonatoAccesoService accesoService;
    @Mock
    private ArchivoCarreraService archivoCarreraService;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock
    private EloCalculator eloCalculator;

    @InjectMocks
    private CarreraService carreraService;

    private Carrera carrera;

    @BeforeEach
    void setUp() {
        Campeonato campeonato = Campeonato.builder().id(1L).nombre("Champ").build();
        carrera = Carrera.builder().id(1L).nombre("Race").campeonato(campeonato)
                .estado(EstadoCarrera.FINALIZADA).fecha(LocalDateTime.now().minusDays(1)).build();
        when(carreraRepository.findById(1L)).thenReturn(Optional.of(carrera));
    }

    @Test
    void noSeBorraUnaCarreraConResultados() {
        when(resultadoCarreraRepository.existsByCarrera_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> carreraService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene resultados");
        verify(carreraRepository, never()).delete(any(Carrera.class));
    }

    @Test
    void seBorraUnaCarreraSinResultados() {
        when(resultadoCarreraRepository.existsByCarrera_Id(1L)).thenReturn(false);

        carreraService.delete(1L);

        verify(carreraRepository).delete(carrera);
    }

    @Test
    void noSeMueveDeCampeonatoUnaCarreraConResultados() {
        when(resultadoCarreraRepository.existsByCarrera_Id(1L)).thenReturn(true);
        CarreraRequest request = new CarreraRequest("Race", carrera.getFecha(), null, null, 2L,
                null, null, null, null, null, null, null);

        assertThatThrownBy(() -> carreraService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("otro campeonato");
        verify(carreraRepository, never()).save(any());
    }
}
