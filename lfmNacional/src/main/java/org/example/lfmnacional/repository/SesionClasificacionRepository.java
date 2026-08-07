package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionClasificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesionClasificacionRepository extends JpaRepository<SesionClasificacion, Long> {

    List<SesionClasificacion> findByCarrera_IdOrderByTiempoAsc(Long carreraId);

    Optional<SesionClasificacion> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);
}
