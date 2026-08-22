package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.EloSancion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EloSancionRepository extends JpaRepository<EloSancion, Long> {

    List<EloSancion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<EloSancion> findByUsuario_IdAndCarrera_Id(Long usuarioId, Long carreraId);

    void deleteByCarrera_Id(Long carreraId);
}
