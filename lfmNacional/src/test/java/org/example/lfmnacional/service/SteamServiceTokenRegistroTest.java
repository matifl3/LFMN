package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * El GUID de Steam solo se puede asociar a una cuenta con un token emitido tras un
 * OpenID valido. Antes el front mandaba el GUID suelto y cualquiera podia registrar
 * el Steam de otro piloto (y quedarse con sus resultados importados).
 */
class SteamServiceTokenRegistroTest {

    private static final String GUID = "76561198000000001";

    private JwtUtil jwtUtil;
    private SteamService steamService;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-test-secret-test-secret-0123456789", 60);
        steamService = new SteamService(mock(UsuarioRepository.class), jwtUtil);
    }

    @Test
    void tokenDeRegistroDevuelveElGuidVerificado() {
        String token = steamService.generarTokenRegistro(GUID);

        assertThat(steamService.extraerGuidRegistro(token)).isEqualTo(GUID);
    }

    @Test
    void rechazaUnGuidSueltoEnLugarDelToken() {
        assertThatThrownBy(() -> steamService.extraerGuidRegistro(GUID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rechazaTokenNuloOVacio() {
        assertThatThrownBy(() -> steamService.extraerGuidRegistro(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> steamService.extraerGuidRegistro(" "))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rechazaTokenDeSesion() {
        Usuario usuario = Usuario.builder().id(1L).email("a@b.com").nombrePiloto("A")
                .rol(Rol.USUARIO).tokenVersion(0).build();

        assertThatThrownBy(() -> steamService.extraerGuidRegistro(jwtUtil.generarToken(usuario)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rechazaTokenDeOtroScopeAunqueElSubjectParezcaUnGuid() {
        String reset = jwtUtil.generarToken(GUID, Map.of("scope", "reset"), 10);

        assertThatThrownBy(() -> steamService.extraerGuidRegistro(reset))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rechazaTokenFirmadoConOtroSecreto() {
        JwtUtil otro = new JwtUtil("otro-secreto-otro-secreto-otro-secreto-0123", 60);
        String falso = otro.generarToken(GUID, Map.of("scope", "steam-registro"), 10);

        assertThatThrownBy(() -> steamService.extraerGuidRegistro(falso))
                .isInstanceOf(BusinessException.class);
    }
}
