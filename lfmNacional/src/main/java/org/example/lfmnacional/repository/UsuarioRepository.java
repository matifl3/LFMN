package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByGuidSteam(String guidSteam);

    boolean existsByEmail(String email);

    boolean existsByGuidSteam(String guidSteam);

    List<Usuario> findByRol(Rol rol);
}
