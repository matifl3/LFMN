package org.example.lfmnacional.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Logro;
import org.example.lfmnacional.entity.Recompensa;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.TipoCondicionLogro;
import org.example.lfmnacional.enums.TipoRecompensa;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.LogroRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@lfm.local";
    /** Password historica del admin sembrado: si sigue puesta en la BD, es publica. */
    private static final String ADMIN_PASSWORD_DEFAULT = "admin123";
    private static final String DEMO_PASSWORD = "piloto123";
    private static final List<String> DEMO_EMAILS =
            List.of("piloto1@lfm.local", "piloto2@lfm.local", "organizador@lfm.local");

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final LogroRepository logroRepository;
    private final PasswordEncoder passwordEncoder;

    /** Password del admin sembrado. En prod sale de ADMIN_PASSWORD (sin default). */
    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    /** Pilotos de prueba con password conocida: solo fuera de prod. */
    @Value("${app.seed.usuarios-demo:false}")
    private boolean usuariosDemo;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategorias();
        seedUsuarios();
        seedCampeonatos();
        seedLogros();
    }

    /**
     * Crea el admin inicial con {@code app.seed.admin-password}. Si el admin ya
     * existe pero conserva la password por defecto (publica en el repo), la rota a
     * la configurada; si no hay ninguna configurada, lo deshabilita e invalida sus
     * tokens para que nadie entre con {@code admin123}. Un admin que ya cambio su
     * password no se toca.
     */
    private void seedAdmin() {
        Optional<Usuario> existente = usuarioRepository.findByEmail(ADMIN_EMAIL);
        if (existente.isPresent()) {
            Usuario admin = existente.get();
            if (adminPassword.equals(ADMIN_PASSWORD_DEFAULT)
                    || !passwordEncoder.matches(ADMIN_PASSWORD_DEFAULT, admin.getPassword())) {
                return;
            }
            if (adminPassword.isBlank()) {
                admin.setHabilitado(false);
                log.error("{} conserva la password por defecto y ADMIN_PASSWORD no esta configurada: "
                        + "la cuenta queda deshabilitada. Configura ADMIN_PASSWORD y reinicia.", ADMIN_EMAIL);
            } else {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setHabilitado(true);
                log.warn("{} tenia la password por defecto: se reemplazo por ADMIN_PASSWORD.", ADMIN_EMAIL);
            }
            invalidarTokens(admin);
            usuarioRepository.save(admin);
            return;
        }
        if (adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD no configurada: no se crea el admin inicial {}.", ADMIN_EMAIL);
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(adminPassword))
                .nombrePiloto("Admin LFM")
                .rol(Rol.ADMIN)
                .elo(1500)
                .safetyRating(100)
                .fechaRegistro(LocalDateTime.now())
                .build());
    }

    private void seedCategorias() {
        List<Categoria> categorias = List.of(
                Categoria.builder().nombre("TC").descripcion("Turismo Carretera").eloMinimo(7000).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TCP").descripcion("Turismo Carretera Pista").eloMinimo(6000).eloMaximo(8000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TCPK").descripcion("Turismo Carretera Pick Up").eloMinimo(7000).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TP C1").descripcion("Turismo Pista Clase 1").eloMinimo(100).eloMaximo(6000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TP C2").descripcion("Turismo Pista Clase 2").eloMinimo(700).eloMaximo(6000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TP C3").descripcion("Turismo Pista Clase 3").eloMinimo(2000).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TN").descripcion("Turismo Nacional").eloMinimo(4000).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("F3").descripcion("Formula 3").eloMinimo(0).eloMaximo(8998).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("F2").descripcion("Formula 2").eloMinimo(1000).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("TC2000").descripcion("Turismo Carretera 2000").eloMinimo(6001).eloMaximo(9000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("LFM Pro").descripcion("Categoria de alto nivel, elo 2000 a 5000").eloMinimo(2000).eloMaximo(5000).setupAbierto(false).setupFijo(false).build(),
                Categoria.builder().nombre("LFM Open").descripcion("Categoria abierta para todos los pilotos").eloMinimo(0).eloMaximo(9999).setupAbierto(true).setupFijo(false).build()
        );
        for (Categoria cat : categorias) {
            if (!categoriaRepository.existsByNombre(cat.getNombre())) {
                categoriaRepository.save(cat);
            }
        }
    }

    private void seedUsuarios() {
        if (!usuariosDemo) {
            deshabilitarUsuariosDemo();
            return;
        }
        if (!usuarioRepository.existsByEmail("piloto1@lfm.local")) {
            usuarioRepository.save(Usuario.builder()
                    .email("piloto1@lfm.local")
                    .password(passwordEncoder.encode("piloto123"))
                    .nombrePiloto("Piloto Uno")
                    .rol(Rol.USUARIO)
                    .elo(1500)
                    .safetyRating(100)
                    .fechaRegistro(LocalDateTime.now())
                    .build());
        }
        if (!usuarioRepository.existsByEmail("piloto2@lfm.local")) {
            usuarioRepository.save(Usuario.builder()
                    .email("piloto2@lfm.local")
                    .password(passwordEncoder.encode("piloto123"))
                    .nombrePiloto("Piloto Dos")
                    .rol(Rol.USUARIO)
                    .elo(1500)
                    .safetyRating(100)
                    .fechaRegistro(LocalDateTime.now())
                    .build());
        }
        if (!usuarioRepository.existsByEmail("organizador@lfm.local")) {
            usuarioRepository.save(Usuario.builder()
                    .email("organizador@lfm.local")
                    .password(passwordEncoder.encode("piloto123"))
                    .nombrePiloto("Organizador LFM")
                    .rol(Rol.ADMIN_CAMPEONATO)
                    .elo(1500)
                    .safetyRating(100)
                    .fechaRegistro(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Fuera de dev los pilotos de prueba no se crean, y los que ya existan con la
     * password conocida quedan deshabilitados (organizador@ es ADMIN_CAMPEONATO).
     */
    private void deshabilitarUsuariosDemo() {
        for (String email : DEMO_EMAILS) {
            usuarioRepository.findByEmail(email)
                    .filter(Usuario::isHabilitado)
                    .filter(u -> passwordEncoder.matches(DEMO_PASSWORD, u.getPassword()))
                    .ifPresent(u -> {
                        u.setHabilitado(false);
                        invalidarTokens(u);
                        usuarioRepository.save(u);
                        log.warn("Usuario de prueba {} con password conocida: deshabilitado.", email);
                    });
        }
    }

    private static void invalidarTokens(Usuario usuario) {
        usuario.setTokenVersion((usuario.getTokenVersion() != null ? usuario.getTokenVersion() : 0) + 1);
    }

    private void seedCampeonatos() {
        for (Categoria cat : categoriaRepository.findAll()) {
            if (campeonatoRepository.findByCategoria_Id(cat.getId()).isEmpty()) {
                campeonatoRepository.save(Campeonato.builder()
                        .nombre(cat.getNombre() + " Championship 2026")
                        .temporada("2026")
                        .categoria(cat)
                        .estado(EstadoCampeonato.ACTIVO)
                        .sistemaPuntos("F1 Standard")
                        .build());
            }
        }
    }

    private static final java.util.Set<String> DESCRIPCIONES_VIEJAS =
            java.util.Set.of("Completa tu primera carrera", "Completa 10 carreras");

    private void seedLogros() {
        // CARRERAS cuenta tambien las que no terminaste: la descripcion decia
        // "Completa", que es CARRERAS_COMPLETADAS.
        seedLogro("Primera carrera", "Corre tu primera carrera",
                TipoCondicionLogro.CARRERAS, 1, "flag",
                "Primer logro de la liga");
        seedLogro("Piloto fiel", "Corre 10 carreras",
                TipoCondicionLogro.CARRERAS, 10, "event",
                "Medalla de constancia por 10 carreras");
        seedLogro("Primera victoria", "Gana tu primera carrera",
                TipoCondicionLogro.VICTORIAS, 1, "emoji_events",
                "Trofeo de tu primera victoria");
        seedLogro("Podio x3", "Subite al podio en 3 carreras",
                TipoCondicionLogro.PODIOS, 3, "military_tech",
                "Reconocimiento por 3 podios");
        seedLogro("Rey de la clasificacion", "Logra 5 poles",
                TipoCondicionLogro.POLES, 5, "timer",
                "Distincion por 5 poles");
        seedLogro("Vuelta rapida", "Registra tu primera vuelta rapida",
                TipoCondicionLogro.VUELTAS_RAPIDAS, 1, "bolt",
                "Por marcar la vuelta mas rapida");
        seedLogro("Campeon en progreso", "Alcanza 1800 de Elo",
                TipoCondicionLogro.ELO, 1800, "trending_up",
                "Por superar los 1800 puntos de Elo");
    }

    private void seedLogro(String nombre, String descripcion,
                           TipoCondicionLogro tipoCondicion, int valorCondicion,
                           String icono, String recompensaDescripcion) {
        Optional<Logro> existente = logroRepository.findByNombre(nombre);
        if (existente.isEmpty()) {
            Logro logro = logroRepository.save(Logro.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .tipoCondicion(tipoCondicion)
                    .valorCondicion(valorCondicion)
                    .icono(icono)
                    .build());
            if (recompensaDescripcion != null) {
                Recompensa recompensa = Recompensa.builder()
                        .logro(logro)
                        .descripcion(recompensaDescripcion)
                        .tipo(TipoRecompensa.VIRTUAL)
                        .build();
                logro.getRecompensas().add(recompensa);
                logroRepository.save(logro);
            }
        } else {
            Logro logro = existente.get();
            boolean cambio = false;
            if (!icono.equals(logro.getIcono())) {
                logro.setIcono(icono);
                cambio = true;
            }
            // Solo pisa la descripcion si sigue siendo la vieja del seed: si un
            // admin la edito desde el panel, se respeta.
            if (logro.getDescripcion() != null && DESCRIPCIONES_VIEJAS.contains(logro.getDescripcion())) {
                logro.setDescripcion(descripcion);
                cambio = true;
            }
            if (cambio) {
                logroRepository.save(logro);
            }
        }
    }
}
