package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionClasificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SesionClasificacionRepository extends JpaRepository<SesionClasificacion, Long> {

    List<SesionClasificacion> findByCarrera_IdOrderByTiempoAsc(Long carreraId);
}
