package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.enums.EstadoApelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {

    List<Apelacion> findAllByOrderByFechaDesc();

    List<Apelacion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Apelacion> findByEstadoOrderByFechaAsc(EstadoApelacion estado);

    List<Apelacion> findByUsuario_Id(Long usuarioId);

    List<Apelacion> findBySancion_Id(Long sancionId);

    List<Apelacion> findByEstado(EstadoApelacion estado);

    boolean existsBySancion_IdAndUsuario_Id(Long sancionId, Long usuarioId);

    boolean existsBySancion_Id(Long sancionId);

    @Modifying
    @Query(value = "DELETE FROM apelacion WHERE sancion_id IN (SELECT id FROM sancion WHERE carrera_id = ?1)", nativeQuery = true)
    void deleteByCarreraId(Long carreraId);
}
