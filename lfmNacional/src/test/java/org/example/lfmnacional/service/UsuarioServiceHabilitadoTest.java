package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.usuario.LoginRequest;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceHabilitadoTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EloSancionRepository eloSancionRepository;
    @Mock
    private SafetyRatingSancionRepository safetyRatingSancionRepository;
    @Mock
    private ResultadoCarreraRepository resultadoCarreraRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).email("piloto@lbm.com").nombrePiloto("Piloto").rol(Rol.USUARIO)
                .elo(1500).safetyRating(100).tokenVersion(0).build();
        usuario.setPassword("hash");
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        lenient().when(jwtUtil.generarToken(usuario)).thenReturn("jwt-token");
    }

    @Test
    void deshabilitarUsuarioInvalidaToken() {
        var response = usuarioService.updateHabilitado(1L, false);

        assertThat(response.habilitado()).isFalse();
        assertThat(usuario.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void rehabilitarUsuarioNoTocaTokenVersion() {
        usuarioService.updateHabilitado(1L, false);
        var response = usuarioService.updateHabilitado(1L, true);

        assertThat(response.habilitado()).isTrue();
        assertThat(usuario.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void loginUsuarioDeshabilitadoRechaza() {
        usuario.setHabilitado(false);
        when(usuarioRepository.findByEmail("piloto@lbm.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("piloto@lbm.com", "123456")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("deshabilitado");
    }

    @Test
    void loginUsuarioHabilitadoDevuelveToken() {
        when(usuarioRepository.findByEmail("piloto@lbm.com")).thenReturn(Optional.of(usuario));

        var response = usuarioService.login(new LoginRequest("piloto@lbm.com", "123456"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.usuario().habilitado()).isTrue();
    }
}