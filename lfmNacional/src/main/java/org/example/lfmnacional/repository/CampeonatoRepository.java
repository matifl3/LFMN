package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Campeonato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {

    List<Campeonato> findByCategoria_Id(Long categoriaId);

    List<Campeonato> findByEstado(org.example.lfmnacional.enums.EstadoCampeonato estado);
}
