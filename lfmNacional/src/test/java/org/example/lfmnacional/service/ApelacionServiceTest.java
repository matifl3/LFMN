package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.apelacion.ApelacionRequest;
import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApelacionServiceTest {

    @Mock
    private ApelacionRepository apelacionRepository;
    @Mock
    private SancionService sancionService;
    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private ApelacionService apelacionService;

    private Usuario sancionado;
    private Usuario otro;

    @BeforeEach
    void setUp() {
        sancionado = Usuario.builder().id(1L).nombrePiloto("Sancionado").rol(Rol.USUARIO).build();
        otro = Usuario.builder().id(2L).nombrePiloto("Otro").rol(Rol.USUARIO).build();
        when(sancionService.getEntity(10L))
                .thenReturn(Sancion.builder().id(10L).usuario(sancionado).build());
    }

    @Test
    void noSePuedeApelarLaSancionDeOtroPiloto() {
        assertThatThrownBy(() -> apelacionService.create(new ApelacionRequest(10L, "no fui yo"), otro))
                .isInstanceOf(AccessDeniedException.class);
        verify(apelacionRepository, never()).save(any(Apelacion.class));
    }

    @Test
    void elSancionadoPuedeApelarSuSancion() {
        when(apelacionRepository.save(any(Apelacion.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = apelacionService.create(new ApelacionRequest(10L, "fue un racing incident"), sancionado);

        assertThat(r.sancionId()).isEqualTo(10L);
        assertThat(r.usuarioId()).isEqualTo(1L);
    }
}
