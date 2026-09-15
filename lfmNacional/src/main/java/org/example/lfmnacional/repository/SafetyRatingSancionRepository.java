package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SafetyRatingSancionRepository extends JpaRepository<SafetyRatingSancion, Long> {

    List<SafetyRatingSancion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<SafetyRatingSancion> findByUsuario_IdAndCarrera_Id(Long usuarioId, Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from SafetyRatingSancion s where s.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
