package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoMiembro;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampeonatoAccesoServiceTest {

    @Mock
    private CampeonatoMiembroRepository miembroRepository;

    @InjectMocks
    private CampeonatoAccesoService acceso;

    private Usuario adminGlobal;
    private Usuario dueno;
    private Usuario duenoAjeno;
    private Usuario comisario;
    private Usuario piloto;
    private Campeonato publico;
    private Campeonato privado;
    private Campeonato privadoAjeno;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nombre("GT3").build();
        adminGlobal = Usuario.builder().id(1L).nombrePiloto("Admin").rol(Rol.ADMIN).build();
        dueno = Usuario.builder().id(2L).nombrePiloto("Dueno1").rol(Rol.ADMIN_CAMPEONATO).build();
        duenoAjeno = Usuario.builder().id(3L).nombrePiloto("Dueno2").rol(Rol.ADMIN_CAMPEONATO).build();
        comisario = Usuario.builder().id(4L).nombrePiloto("Comisario").rol(Rol.COMISARIO).build();
        piloto = Usuario.builder().id(5L).nombrePiloto("Piloto").rol(Rol.USUARIO).build();
        publico = Campeonato.builder().id(10L).nombre("Liga Abierta").categoria(categoria)
                .visibilidad(VisibilidadCampeonato.PUBLICO).build();
        privado = Campeonato.builder().id(11L).nombre("Copa Privada").categoria(categoria)
                .visibilidad(VisibilidadCampeonato.PRIVADO).admin(dueno).build();
        privadoAjeno = Campeonato.builder().id(12L).nombre("Otra Privada").categoria(categoria)
                .visibilidad(VisibilidadCampeonato.PRIVADO).admin(duenoAjeno).build();
    }

    @Test
    void adminGlobalAdministraTodo() {
        assertThat(acceso.administra(adminGlobal, privado)).isTrue();
        assertThat(acceso.administra(adminGlobal, privadoAjeno)).isTrue();
    }

    @Test
    void adminCampeonatoSoloAdministraLosSuyos() {
        assertThat(acceso.administra(dueno, privado)).isTrue();
        assertThat(acceso.administra(dueno, privadoAjeno)).isFalse();
        assertThat(acceso.administra(dueno, publico)).isFalse();
    }

    @Test
    void pilotoNoAdministraNada() {
        assertThat(acceso.administra(piloto, privado)).isFalse();
        assertThat(acceso.administra(piloto, publico)).isFalse();
    }

    @Test
    void enUnPublicoNoHaceFaltaSerMiembro() {
        assertThat(acceso.veContenido(piloto, publico)).isTrue();
        assertThat(acceso.veContenido(null, publico)).isTrue();
    }

    @Test
    void noMiembroNoVeElContenidoDeUnPrivado() {
        when(miembroRepository.existsByCampeonato_IdAndUsuario_Id(11L, piloto.getId())).thenReturn(false);

        assertThat(acceso.veContenido(piloto, privado)).isFalse();
    }

    @Test
    void miembroVeElContenidoDeUnPrivado() {
        when(miembroRepository.existsByCampeonato_IdAndUsuario_Id(11L, piloto.getId())).thenReturn(true);

        assertThat(acceso.veContenido(piloto, privado)).isTrue();
    }

    @Test
    void elDuenoYElComisarioTambienVenElContenidoPrivado() {
        assertThat(acceso.veContenido(dueno, privado)).isTrue();
        assertThat(acceso.veContenido(comisario, privado)).isTrue();
    }

    @Test
    void alAnonimoNoSeLeConsultaNiLaMembresia() {
        assertThat(acceso.veContenido(null, privado)).isFalse();
        verify(miembroRepository, never()).existsByCampeonato_IdAndUsuario_Id(11L, null);
    }

    @Test
    void idsDeMembresiaEnUnaSolaQuery() {
        when(miembroRepository.findByUsuario_Id(piloto.getId())).thenReturn(List.of(
                CampeonatoMiembro.builder().campeonato(privado).usuario(piloto).build(),
                CampeonatoMiembro.builder().campeonato(publico).usuario(piloto).build()));

        Set<Long> ids = acceso.campeonatoIdsDondeEsMiembro(piloto.getId());

        assertThat(ids).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void sinUsuarioNoHayIdsDeMembresia() {
        assertThat(acceso.campeonatoIdsDondeEsMiembro(null)).isEmpty();
    }

    @Test
    void exigirVeContenidoFallaConMensajeDeCampeonatoPrivado() {
        when(miembroRepository.existsByCampeonato_IdAndUsuario_Id(11L, piloto.getId())).thenReturn(false);

        assertThatThrownBy(() -> acceso.exigirVeContenido(piloto, privado))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("es privado");
    }

    @Test
    void exigirAdministraFallaParaElDuenoDeOtroCampeonato() {
        assertThatThrownBy(() -> acceso.exigirAdministra(dueno, privadoAjeno))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No administras");
    }

    @Test
    void exigirVeCarreraDelegaEnElCampeonatoDeLaCarrera() {
        Carrera carrera = Carrera.builder().id(30L).nombre("Privada 1")
                .campeonato(privado).estado(EstadoCarrera.PROGRAMADA).build();
        when(miembroRepository.existsByCampeonato_IdAndUsuario_Id(11L, piloto.getId())).thenReturn(false);

        assertThat(acceso.veCarrera(piloto, carrera)).isFalse();
        assertThatThrownBy(() -> acceso.exigirVeCarrera(piloto, carrera))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no sos miembro");
    }
}
