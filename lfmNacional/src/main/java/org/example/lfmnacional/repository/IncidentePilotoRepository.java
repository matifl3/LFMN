package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.IncidentePiloto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentePilotoRepository extends JpaRepository<IncidentePiloto, Long> {

    List<IncidentePiloto> findByIncidente_Id(Long incidenteId);

    List<IncidentePiloto> findByUsuario_Id(Long usuarioId);

    @Modifying
    @Query(value = "DELETE FROM incidente_piloto WHERE incidente_id IN (SELECT id FROM incidente WHERE carrera_id = ?1)", nativeQuery = true)
    void deleteByCarreraId(Long carreraId);
}
