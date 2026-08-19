package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ResolucionIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ResolucionIncidenteRepository extends JpaRepository<ResolucionIncidente, Long> {

    Optional<ResolucionIncidente> findByIncidente_Id(Long incidenteId);

    java.util.List<ResolucionIncidente> findByComisario_Id(Long comisarioId);

    @Modifying
    @Query(value = "DELETE FROM resolucion_incidente WHERE incidente_id IN (SELECT id FROM incidente WHERE carrera_id = ?1)", nativeQuery = true)
    void deleteByCarreraId(Long carreraId);
}
