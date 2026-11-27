package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.incidente.ResolucionRequest;
import org.example.lfmnacional.dto.incidente.VotoRequest;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VotoComisario;
import org.example.lfmnacional.enums.DecisionComisario;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.ResolucionIncidenteRepository;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Quien esta involucrado en un incidente no puede votarlo ni resolverlo. */
@ExtendWith(MockitoExtension.class)
class IncidenteConflictoInteresTest {

    @Mock
    private IncidenteRepository incidenteRepository;
    @Mock
    private IncidentePilotoRepository incidentePilotoRepository;
    @Mock
    private VotoComisarioRepository votoComisarioRepository;
    @Mock
    private ResolucionIncidenteRepository resolucionIncidenteRepository;

    @InjectMocks
    private IncidenteService incidenteService;

    private Usuario reportante;
    private Usuario comisario;

    @BeforeEach
    void setUp() {
        reportante = Usuario.builder().id(1L).nombrePiloto("Reportante").rol(Rol.COMISARIO).build();
        comisario = Usuario.builder().id(2L).nombrePiloto("Comisario").rol(Rol.COMISARIO).build();
        Incidente incidente = Incidente.builder().id(50L).reportante(reportante)
                .estado(EstadoIncidente.PENDIENTE).build();
        when(incidenteRepository.findById(50L)).thenReturn(Optional.of(incidente));
        lenient().when(incidentePilotoRepository.existsByIncidente_IdAndUsuario_Id(any(), any())).thenReturn(false);
    }

    private VotoRequest voto() {
        return new VotoRequest(DecisionComisario.A_FAVOR, "comentario");
    }

    @Test
    void elReportanteNoPuedeVotarSuPropioIncidente() {
        assertThatThrownBy(() -> incidenteService.votar(50L, voto(), reportante))
                .isInstanceOf(AccessDeniedException.class);
        verify(votoComisarioRepository, never()).save(any(VotoComisario.class));
    }

    @Test
    void unPilotoInvolucradoNoPuedeVotarNiResolver() {
        when(incidentePilotoRepository.existsByIncidente_IdAndUsuario_Id(50L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> incidenteService.votar(50L, voto(), comisario))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> incidenteService.guardarResolucion(50L,
                new ResolucionRequest("explicacion", null), comisario))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void unComisarioAjenoPuedeVotar() {
        when(votoComisarioRepository.findByIncidente_IdAndComisario_Id(50L, 2L)).thenReturn(Optional.empty());
        when(votoComisarioRepository.save(any(VotoComisario.class))).thenAnswer(inv -> inv.getArgument(0));

        incidenteService.votar(50L, voto(), comisario);

        verify(votoComisarioRepository).save(any(VotoComisario.class));
    }
}
