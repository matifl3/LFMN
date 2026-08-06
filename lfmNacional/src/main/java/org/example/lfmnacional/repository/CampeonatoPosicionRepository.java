package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampeonatoPosicionRepository extends JpaRepository<CampeonatoPosicion, Long> {

    List<CampeonatoPosicion> findByCampeonato_IdOrderByPuntosDesc(Long campeonatoId);

    Optional<CampeonatoPosicion> findByCampeonato_IdAndUsuario_Id(Long campeonatoId, Long usuarioId);
}
