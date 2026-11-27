package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.usuario.SteamRegistroRequest;
import org.example.lfmnacional.dto.usuario.UsuarioResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceSeguridadTest {

    private static final String GUID = "76561198000000001";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private SteamService steamService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario piloto;

    @BeforeEach
    void setUp() {
        piloto = Usuario.builder()
                .id(1L).email("piloto@lfm.com").nombrePiloto("Piloto").guidSteam(GUID)
                .rol(Rol.USUARIO).elo(1500).safetyRating(100).tokenVersion(0).build();
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(piloto));
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registroSteamTomaElGuidDelTokenVerificado() {
        when(steamService.extraerGuidRegistro("token-ok")).thenReturn(GUID);

        usuarioService.registrarSteam(new SteamRegistroRequest("nuevo@lfm.com", "Nuevo", "token-ok"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getGuidSteam()).isEqualTo(GUID);
    }

    @Test
    void registroSteamConTokenInvalidoNoCreaLaCuenta() {
        when(steamService.extraerGuidRegistro(GUID))
                .thenThrow(new BusinessException("El ingreso con Steam expiro o es invalido"));

        assertThatThrownBy(() -> usuarioService.registrarSteam(
                new SteamRegistroRequest("nuevo@lfm.com", "Nuevo", GUID)))
                .isInstanceOf(BusinessException.class);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void perfilPublicoNoExponeDatosPrivadosAAnonimos() {
        UsuarioResponse r = usuarioService.getById(1L, null);

        assertThat(r.email()).isNull();
        assertThat(r.guidSteam()).isNull();
        assertThat(r.passwordEstablecida()).isNull();
        assertThat(r.nombrePiloto()).isEqualTo("Piloto");
        assertThat(r.elo()).isEqualTo(1500);
    }

    @Test
    void perfilPublicoNoExponeDatosPrivadosAOtroUsuario() {
        Usuario otro = Usuario.builder().id(2L).rol(Rol.USUARIO).build();

        assertThat(usuarioService.getById(1L, otro).email()).isNull();
    }

    @Test
    void elPropioUsuarioYElAdminVenElPerfilCompleto() {
        Usuario admin = Usuario.builder().id(9L).rol(Rol.ADMIN).build();

        assertThat(usuarioService.getById(1L, piloto).email()).isEqualTo("piloto@lfm.com");
        assertThat(usuarioService.getById(1L, admin).email()).isEqualTo("piloto@lfm.com");
        assertThat(usuarioService.getById(1L, admin).guidSteam()).isEqualTo(GUID);
    }
}
