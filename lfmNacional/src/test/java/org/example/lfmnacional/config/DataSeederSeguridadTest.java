package org.example.lfmnacional.config;

import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.LogroRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El seeder corre tambien en prod: no puede dejar cuentas con passwords que estan
 * escritas en el repo (admin123 / piloto123).
 */
@ExtendWith(MockitoExtension.class)
class DataSeederSeguridadTest {

    private static final String ADMIN = "admin@lfm.local";

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private CampeonatoRepository campeonatoRepository;
    @Mock
    private LogroRepository logroRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(usuarioRepository, categoriaRepository, campeonatoRepository,
                logroRepository, encoder);
        lenient().when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    }

    private void configurar(String adminPassword, boolean usuariosDemo) {
        ReflectionTestUtils.setField(seeder, "adminPassword", adminPassword);
        ReflectionTestUtils.setField(seeder, "usuariosDemo", usuariosDemo);
    }

    private Usuario usuario(String email, String password, Rol rol) {
        Usuario u = Usuario.builder().id(1L).email(email).nombrePiloto(email).rol(rol)
                .tokenVersion(0).build();
        u.setPassword(encoder.encode(password));
        return u;
    }

    @Test
    void sinAdminPasswordNoCreaElAdmin() {
        configurar("", false);

        ReflectionTestUtils.invokeMethod(seeder, "seedAdmin");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void creaElAdminConLaPasswordConfigurada() {
        configurar("s3cret-largo", false);

        ReflectionTestUtils.invokeMethod(seeder, "seedAdmin");

        ArgumentCaptor<Usuario> c = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(c.capture());
        assertThat(encoder.matches("s3cret-largo", c.getValue().getPassword())).isTrue();
        assertThat(encoder.matches("admin123", c.getValue().getPassword())).isFalse();
    }

    @Test
    void adminConPasswordPorDefectoSinAdminPasswordQuedaDeshabilitado() {
        configurar("", false);
        Usuario admin = usuario(ADMIN, "admin123", Rol.ADMIN);
        when(usuarioRepository.findByEmail(ADMIN)).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(seeder, "seedAdmin");

        assertThat(admin.isHabilitado()).isFalse();
        assertThat(admin.getTokenVersion()).isEqualTo(1);
        verify(usuarioRepository).save(admin);
    }

    @Test
    void adminConPasswordPorDefectoSeRotaAAdminPassword() {
        configurar("s3cret-largo", false);
        Usuario admin = usuario(ADMIN, "admin123", Rol.ADMIN);
        when(usuarioRepository.findByEmail(ADMIN)).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(seeder, "seedAdmin");

        assertThat(encoder.matches("s3cret-largo", admin.getPassword())).isTrue();
        assertThat(admin.isHabilitado()).isTrue();
        assertThat(admin.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void adminQueYaCambioSuPasswordNoSeToca() {
        configurar("", false);
        Usuario admin = usuario(ADMIN, "la-que-eligio", Rol.ADMIN);
        when(usuarioRepository.findByEmail(ADMIN)).thenReturn(Optional.of(admin));

        ReflectionTestUtils.invokeMethod(seeder, "seedAdmin");

        assertThat(admin.isHabilitado()).isTrue();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void sinUsuariosDemoSeDeshabilitanLosDePasswordConocida() {
        configurar("", false);
        Usuario organizador = usuario("organizador@lfm.local", "piloto123", Rol.ADMIN_CAMPEONATO);
        when(usuarioRepository.findByEmail("organizador@lfm.local")).thenReturn(Optional.of(organizador));

        ReflectionTestUtils.invokeMethod(seeder, "seedUsuarios");

        assertThat(organizador.isHabilitado()).isFalse();
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }
}
