package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionClasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SesionClasificacionRepository extends JpaRepository<SesionClasificacion, Long> {

    List<SesionClasificacion> findByCarrera_IdOrderByTiempoAsc(Long carreraId);

    List<SesionClasificacion> findByUsuario_IdOrderByTiempoAsc(Long usuarioId);

    Optional<SesionClasificacion> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from SesionClasificacion s where s.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
