package org.example.lfmnacional.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final LogroRepository logroRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategorias();
        seedUsuarios();
        seedCampeonatos();
        seedLogros();
    }

    private void seedAdmin() {
        if (!usuarioRepository.existsByEmail("admin@lfm.local")) {
            usuarioRepository.save(Usuario.builder()
                    .email("admin@lfm.local")
                    .password(passwordEncoder.encode("admin123"))
                    .nombrePiloto("Admin LFM")
                    .rol(Rol.ADMIN)
                    .elo(1500)
                    .safetyRating(100)
                    .fechaRegistro(LocalDateTime.now())
                    .build());
        }
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

    private void seedLogros() {
        seedLogro("Primera carrera", "Completa tu primera carrera",
                TipoCondicionLogro.CARRERAS, 1, "\uD83C\uDFC1",
                "Primer logro de la liga");
        seedLogro("Piloto fiel", "Completa 10 carreras",
                TipoCondicionLogro.CARRERAS, 10, "\uD83D\uDCC5",
                "Medalla de constancia por 10 carreras");
        seedLogro("Primera victoria", "Gana tu primera carrera",
                TipoCondicionLogro.VICTORIAS, 1, "\uD83C\uDFC6",
                "Trofeo de tu primera victoria");
        seedLogro("Podio x3", "Subite al podio en 3 carreras",
                TipoCondicionLogro.PODIOS, 3, "\uD83E\uDD49",
                "Reconocimiento por 3 podios");
        seedLogro("Rey de la clasificacion", "Logra 5 poles",
                TipoCondicionLogro.POLES, 5, "\u23F1\uFE0F",
                "Distincion por 5 poles");
        seedLogro("Vuelta rapida", "Registra tu primera vuelta rapida",
                TipoCondicionLogro.VUELTAS_RAPIDAS, 1, "\u26A1",
                "Por marcar la vuelta mas rapida");
        seedLogro("Campeon en progreso", "Alcanza 1800 de Elo",
                TipoCondicionLogro.ELO, 1800, "\uD83D\uDCC8",
                "Por superar los 1800 puntos de Elo");
    }

    private void seedLogro(String nombre, String descripcion,
                           TipoCondicionLogro tipoCondicion, int valorCondicion,
                           String icono, String recompensaDescripcion) {
        if (!logroRepository.existsByNombre(nombre)) {
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
        }
    }
}
