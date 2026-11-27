package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.usuario.LoginRequest;
import org.example.lfmnacional.dto.usuario.SteamRegistroRequest;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.example.lfmnacional.security.LoginIntentos;
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

/** Registro, login y Steam: lo que antes se probaba contra UsuarioService. */
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

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

    @InjectMocks
    private AutenticacionService autenticacionService;

    private Usuario piloto;

    @BeforeEach
    void setUp() {
        piloto = Usuario.builder()
                .id(1L).email("piloto@lfm.com").nombrePiloto("Piloto")
                .rol(Rol.USUARIO).elo(1500).safetyRating(100).tokenVersion(0).build();
        piloto.setPassword("hash");
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(passwordEncoder.matches("buena", "hash")).thenReturn(true);
        lenient().when(jwtUtil.generarToken(any(Usuario.class))).thenReturn("jwt-token");
    }

    // ---- Steam: el GUID sale del token verificado, nunca del cliente

    @Test
    void registroSteamTomaElGuidDelTokenVerificado() {
        when(steamService.extraerGuidRegistro("token-ok")).thenReturn(GUID);

        autenticacionService.registrarSteam(new SteamRegistroRequest("nuevo@lfm.com", "Nuevo", "token-ok"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getGuidSteam()).isEqualTo(GUID);
    }

    @Test
    void registroSteamConTokenInvalidoNoCreaLaCuenta() {
        when(steamService.extraerGuidRegistro(GUID))
                .thenThrow(new BusinessException("El ingreso con Steam expiro o es invalido"));

        assertThatThrownBy(() -> autenticacionService.registrarSteam(
                new SteamRegistroRequest("nuevo@lfm.com", "Nuevo", GUID)))
                .isInstanceOf(BusinessException.class);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ---- login: bloqueo por cuenta y sin enumeracion de emails

    @Test
    void loginCorrectoDevuelveTokenYPerfilCompleto() {
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));

        var response = autenticacionService.login(new LoginRequest("piloto@lfm.com", "buena"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.usuario().email()).isEqualTo("piloto@lfm.com");
    }

    @Test
    void cuentaDeshabilitadaConPasswordCorrectaAvisaQueEstaDeshabilitada() {
        piloto.setHabilitado(false);
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));

        assertThatThrownBy(() -> autenticacionService.login(new LoginRequest("piloto@lfm.com", "buena")))
                .hasMessageContaining("deshabilitado");
    }

    @Test
    void cuentaDeshabilitadaConPasswordIncorrectaNoRevelaQueEstaDeshabilitada() {
        piloto.setHabilitado(false);
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));

        assertThatThrownBy(() -> autenticacionService.login(new LoginRequest("piloto@lfm.com", "mala")))
                .hasMessage("Email o contrasena invalidos");
    }

    @Test
    void emailInexistenteDaElMismoMensajeQuePasswordIncorrecta() {
        when(usuarioRepository.findByEmail("nadie@lfm.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacionService.login(new LoginRequest("nadie@lfm.com", "x")))
                .hasMessage("Email o contrasena invalidos");
    }

    @Test
    void trasCincoFallosLaCuentaSeBloqueaAunqueLuegoLaPasswordSeaCorrecta() {
        when(usuarioRepository.findByEmail("piloto@lfm.com")).thenReturn(Optional.of(piloto));
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> autenticacionService.login(new LoginRequest("piloto@lfm.com", "mala")))
                    .hasMessageContaining("invalidos");
        }

        assertThatThrownBy(() -> autenticacionService.login(new LoginRequest("PILOTO@lfm.com", "buena")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Demasiados intentos");
    }
}
