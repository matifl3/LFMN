package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.EloSancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EloSancionRepository extends JpaRepository<EloSancion, Long> {

    List<EloSancion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<EloSancion> findByUsuario_IdAndCarrera_Id(Long usuarioId, Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from EloSancion e where e.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
