package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.setup.SetupRequest;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Setup;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetupServiceFijoTest {

    @Mock
    private SetupRepository setupRepository;
    @Mock
    private SetupCalificacionRepository setupCalificacionRepository;
    @Mock
    private SetupComentarioRepository setupComentarioRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private SetupService setupService;

    private Categoria fija;
    private Categoria libre;
    private Usuario admin;
    private Usuario piloto;

    @BeforeEach
    void setUp() {
        fija = Categoria.builder().id(1L).nombre("GT3 Oficial").setupFijo(true).build();
        libre = Categoria.builder().id(2L).nombre("GT3").setupFijo(false).build();
        admin = Usuario.builder().id(1L).nombrePiloto("Admin").rol(Rol.ADMIN).build();
        piloto = Usuario.builder().id(2L).nombrePiloto("Piloto").rol(Rol.USUARIO).build();
    }

    private SetupRequest request(Long categoriaId) {
        return new SetupRequest("Mi setup", "Desc", "Termas", "Ferrari 296", null, categoriaId);
    }

    @Test
    void setupFijoRechazaPublicacionDeUsuario() {
        when(categoriaService.getEntity(1L)).thenReturn(fija);

        assertThatThrownBy(() -> setupService.create(request(1L), piloto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("setup fijo");
        verify(setupRepository, never()).save(any(Setup.class));
    }

    @Test
    void setupFijoRechazaSegundoSetupOficial() {
        when(categoriaService.getEntity(1L)).thenReturn(fija);
        when(setupRepository.existsByCategoria_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> setupService.create(request(1L), admin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene publicado su setup oficial");
    }

    @Test
    void setupFijoAdminPublicaElOficial() {
        when(categoriaService.getEntity(1L)).thenReturn(fija);
        when(setupRepository.existsByCategoria_Id(1L)).thenReturn(false);
        when(setupRepository.save(any(Setup.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = setupService.create(request(1L), admin);

        assertThat(response.titulo()).isEqualTo("Mi setup");
        assertThat(response.categoriaId()).isEqualTo(1L);
    }

    @Test
    void categoriaLibrePermitePublicacionDeUsuario() {
        when(categoriaService.getEntity(2L)).thenReturn(libre);
        when(setupRepository.save(any(Setup.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = setupService.create(request(2L), piloto);

        assertThat(response.titulo()).isEqualTo("Mi setup");
    }
}