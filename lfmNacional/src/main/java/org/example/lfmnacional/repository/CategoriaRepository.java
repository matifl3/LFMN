package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEloMinimoLessThanEqualAndEloMaximoGreaterThanEqual(Integer elo, Integer elo2);

    boolean existsByNombre(String nombre);
}
