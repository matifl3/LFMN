package org.example.lfmnacional.config;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Logro;
import org.example.lfmnacional.entity.Recompensa;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.TipoCondicionLogro;
import org.example.lfmnacional.enums.TipoRecompensa;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.example.lfmnacional.repository.LogroRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final LogroRepository logroRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategorias();
        seedLogros();
    }

    private void seedAdmin() {
        if (!usuarioRepository.existsByEmail("admin@lfm.local")) {
            Usuario admin = Usuario.builder()
                    .email("admin@lfm.local")
                    .password(passwordEncoder.encode("admin123"))
                    .nombrePiloto("Admin LFM")
                    .rol(Rol.ADMIN)
                    .elo(1500)
                    .safetyRating(100)
                    .fechaRegistro(LocalDateTime.now())
                    .build();
            usuarioRepository.save(admin);
        }
    }

    private void seedCategorias() {
        if (!categoriaRepository.existsByNombre("LFM Pro")) {
            categoriaRepository.save(Categoria.builder()
                    .nombre("LFM Pro")
                    .descripcion("Categoria de alto nivel, elo 2000 a 5000")
                    .eloMinimo(2000)
                    .eloMaximo(5000)
                    .setupAbierto(false)
                    .setupFijo(false)
                    .build());
        }
        if (!categoriaRepository.existsByNombre("LFM Open")) {
            categoriaRepository.save(Categoria.builder()
                    .nombre("LFM Open")
                    .descripcion("Categoria abierta para todos los pilotos")
                    .eloMinimo(0)
                    .eloMaximo(9999)
                    .setupAbierto(true)
                    .setupFijo(false)
                    .build());
        }
    }

    private void seedLogros() {
        seedLogro("Primera carrera", "Completa tu primera carrera",
                TipoCondicionLogro.CARRERAS, 1, "🏁",
                "Primer logro de la liga");
        seedLogro("Piloto fiel", "Completa 10 carreras",
                TipoCondicionLogro.CARRERAS, 10, "📅",
                "Medalla de constancia por 10 carreras");
        seedLogro("Primera victoria", "Gana tu primera carrera",
                TipoCondicionLogro.VICTORIAS, 1, "🏆",
                "Trofeo de tu primera victoria");
        seedLogro("Podio x3", "Subite al podio en 3 carreras",
                TipoCondicionLogro.PODIOS, 3, "🥉",
                "Reconocimiento por 3 podios");
        seedLogro("Rey de la clasificacion", "Logra 5 poles",
                TipoCondicionLogro.POLES, 5, "⏱️",
                "Distincion por 5 poles");
        seedLogro("Vuelta rapida", "Registra tu primera vuelta rapida",
                TipoCondicionLogro.VUELTAS_RAPIDAS, 1, "⚡",
                "Por marcar la vuelta mas rapida");
        seedLogro("Campeon en progreso", "Alcanza 1800 de Elo",
                TipoCondicionLogro.ELO, 1800, "📈",
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
