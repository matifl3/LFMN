package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Logro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogroRepository extends JpaRepository<Logro, Long> {

    Optional<Logro> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
