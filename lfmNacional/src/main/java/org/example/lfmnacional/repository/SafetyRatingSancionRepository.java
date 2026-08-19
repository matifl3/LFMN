package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafetyRatingSancionRepository extends JpaRepository<SafetyRatingSancion, Long> {

    List<SafetyRatingSancion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<SafetyRatingSancion> findByUsuario_IdAndCarrera_Id(Long usuarioId, Long carreraId);

    void deleteByCarrera_Id(Long carreraId);
}
