package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.usuario.SteamRegistroRequest;
import org.example.lfmnacional.dto.usuario.UsuarioResponse;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.example.lfmnacional.security.LoginIntentos;
import org.example.lfmnacional.dto.usuario.LoginRequest;
import org.example.lfmnacional.dto.usuario.UsuarioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    @Spy
    private LoginIntentos loginIntentos = new LoginIntentos();

    /** Real: los chequeos de dueno/admin no tocan repositorios. */
    @Spy
    private CampeonatoAccesoService accesoService = new CampeonatoAccesoService(null, null, null);

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

    // ---- login: bloqueo por cuenta y sin enumeracion de emails

    @Test
    void trasCincoFallosLaCuentaSeBloqueaAunqueLuegoLaPasswordSeaCorrecta() {
        piloto.setPassword("hash");
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> usuarioService.login(new LoginRequest("piloto@lfm.com", "mala")))
                    .hasMessageContaining("invalidos");
        }

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("PILOTO@lfm.com", "buena")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Demasiados intentos");
    }

    @Test
    void emailInexistenteDaElMismoMensajeQuePasswordIncorrecta() {
        when(usuarioRepository.findByEmail("nadie@lfm.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("nadie@lfm.com", "x")))
                .hasMessage("Email o contrasena invalidos");
    }

    @Test
    void cuentaDeshabilitadaConPasswordIncorrectaNoRevelaQueEstaDeshabilitada() {
        piloto.setPassword("hash");
        piloto.setHabilitado(false);
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("piloto@lfm.com", "mala")))
                .hasMessage("Email o contrasena invalidos");
    }

    // ---- cambiar el email exige la password actual

    @Test
    void cambiarSuEmailSinPasswordActualSeRechaza() {
        piloto.setPassword("hash");
        piloto.setPasswordEstablecida(true);

        assertThatThrownBy(() -> usuarioService.updatePerfil(1L,
                new UsuarioRequest("atacante@mail.com", null, "Piloto"), piloto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contrasena actual");
        assertThat(piloto.getEmail()).isEqualTo("piloto@lfm.com");
    }

    @Test
    void cambiarSuEmailConPasswordActualCorrectaFunciona() {
        piloto.setPassword("hash");
        piloto.setPasswordEstablecida(true);
        when(passwordEncoder.matches("buena", "hash")).thenReturn(true);

        usuarioService.updatePerfil(1L, new UsuarioRequest("nuevo@lfm.com", "buena", "Piloto"), piloto);

        assertThat(piloto.getEmail()).isEqualTo("nuevo@lfm.com");
    }

    @Test
    void elAdminCambiaElEmailDeOtroSinPasswordYElNombreSoloNoLaPide() {
        piloto.setPasswordEstablecida(true);
        Usuario admin = Usuario.builder().id(9L).rol(Rol.ADMIN).build();

        usuarioService.updatePerfil(1L, new UsuarioRequest("piloto@lfm.com", null, "Otro nombre"), piloto);
        usuarioService.updatePerfil(1L, new UsuarioRequest("corregido@lfm.com", null, "Otro nombre"), admin);

        assertThat(piloto.getEmail()).isEqualTo("corregido@lfm.com");
    }
}
