package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.CampeonatoMiembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampeonatoMiembroRepository extends JpaRepository<CampeonatoMiembro, Long> {

    List<CampeonatoMiembro> findByCampeonato_IdOrderByFechaAltaAsc(Long campeonatoId);

    List<CampeonatoMiembro> findByUsuario_Id(Long usuarioId);

    Optional<CampeonatoMiembro> findByCampeonato_IdAndUsuario_Id(Long campeonatoId, Long usuarioId);

    boolean existsByCampeonato_IdAndUsuario_Id(Long campeonatoId, Long usuarioId);

    long countByCampeonato_Id(Long campeonatoId);

    void deleteByCampeonato_IdAndUsuario_Id(Long campeonatoId, Long usuarioId);

    @Modifying
    @Query("delete from CampeonatoMiembro m where m.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Modifying
    @Query("delete from CampeonatoMiembro m where m.campeonato.id = ?1")
    void deleteByCampeonato_Id(Long campeonatoId);
}
