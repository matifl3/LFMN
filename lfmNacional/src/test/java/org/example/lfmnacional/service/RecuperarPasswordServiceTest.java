package org.example.lfmnacional.service;

import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** El link de recuperacion sirve una sola vez. */
class RecuperarPasswordServiceTest {

    private JwtUtil jwtUtil;
    private RecuperarPasswordService service;
    private Usuario usuario;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-test-secret-test-secret-0123456789", 60);
        UsuarioRepository repo = mock(UsuarioRepository.class);
        service = new RecuperarPasswordService(repo, encoder, jwtUtil, null);
        usuario = Usuario.builder().id(7L).email("p@lfm.com").nombrePiloto("P").rol(Rol.USUARIO)
                .tokenVersion(3).build();
        usuario.setPassword(encoder.encode("vieja"));
        when(repo.findById(7L)).thenReturn(Optional.of(usuario));
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /** Igual al que arma {@code solicitar}: scope reset + tokenVersion vigente. */
    private String linkDeReset(int tokenVersion) {
        return jwtUtil.generarToken("7", Map.of("scope", "reset"), 30, tokenVersion);
    }

    @Test
    void elLinkSirveUnaVezYLuegoSeRechaza() {
        String token = linkDeReset(3);

        service.restablecer(token, "nueva-1");
        assertThat(encoder.matches("nueva-1", usuario.getPassword())).isTrue();

        assertThatThrownBy(() -> service.restablecer(token, "nueva-2"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue usado");
        assertThat(encoder.matches("nueva-1", usuario.getPassword())).isTrue();
    }

    @Test
    void unLinkViejoNoSirveSiLaPasswordCambioDespues() {
        String token = linkDeReset(2);

        assertThatThrownBy(() -> service.restablecer(token, "nueva-1"))
                .isInstanceOf(BusinessException.class);
    }
}
