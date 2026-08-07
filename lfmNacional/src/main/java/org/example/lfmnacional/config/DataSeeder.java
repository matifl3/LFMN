package org.example.lfmnacional.config;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.repository.CategoriaRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategorias();
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
}
