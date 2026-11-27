package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.setup.SetupCalificacionRequest;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.entity.SetupCalificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetupCalificacionServiceTest {

    @Mock
    private SetupCalificacionRepository setupCalificacionRepository;
    @Mock
    private SetupService setupService;

    @InjectMocks
    private SetupCalificacionService service;

    private Usuario autor;
    private Usuario otro;
    private Setup setup;

    @BeforeEach
    void setUp() {
        autor = Usuario.builder().id(1L).nombrePiloto("Autor").rol(Rol.USUARIO).build();
        otro = Usuario.builder().id(2L).nombrePiloto("Otro").rol(Rol.USUARIO).build();
        setup = Setup.builder().id(10L).titulo("Monza seco").autor(autor).build();
        when(setupService.getEntity(10L)).thenReturn(setup);
    }

    @Test
    void elAutorNoPuedeCalificarSuPropioSetup() {
        assertThatThrownBy(() -> service.calificar(10L, new SetupCalificacionRequest(5), autor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("propio setup");

        verify(setupCalificacionRepository, never()).save(any());
        verify(setupService, never()).recalcularPromedio(any());
    }

    @Test
    void otroPilotoCalificaYSeRecalculaElPromedio() {
        when(setupCalificacionRepository.findBySetup_IdAndUsuario_Id(10L, 2L)).thenReturn(Optional.empty());
        when(setupCalificacionRepository.save(any(SetupCalificacion.class))).thenAnswer(inv -> {
            SetupCalificacion c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });

        var respuesta = service.calificar(10L, new SetupCalificacionRequest(4), otro);

        assertThat(respuesta.puntaje()).isEqualTo(4);
        verify(setupService).recalcularPromedio(setup);
    }
}
