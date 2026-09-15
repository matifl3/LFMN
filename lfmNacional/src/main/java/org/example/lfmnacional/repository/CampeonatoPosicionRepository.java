package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampeonatoPosicionRepository extends JpaRepository<CampeonatoPosicion, Long> {

    List<CampeonatoPosicion> findByCampeonato_IdOrderByPuntosDesc(Long campeonatoId);

    Optional<CampeonatoPosicion> findByCampeonato_IdAndUsuario_Id(Long campeonatoId, Long usuarioId);

    boolean existsByCampeonato_Id(Long campeonatoId);

    long countByCampeonato_Id(Long campeonatoId);

    @Modifying
    @Query("delete from CampeonatoPosicion c where c.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
