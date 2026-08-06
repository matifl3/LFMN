package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    List<Anuncio> findAllByOrderByFechaDesc();
}
