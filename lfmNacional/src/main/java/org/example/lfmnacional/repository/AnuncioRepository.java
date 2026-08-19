package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    List<Anuncio> findAllByOrderByFechaDesc();

    Optional<Anuncio> findFirstByOrderByFechaDesc();

    Optional<Anuncio> findFirstByDestacadoTrueOrderByFechaDesc();
}
