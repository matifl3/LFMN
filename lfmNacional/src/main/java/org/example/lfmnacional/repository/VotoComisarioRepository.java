package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.VotoComisario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VotoComisarioRepository extends JpaRepository<VotoComisario, Long> {

    List<VotoComisario> findByIncidente_Id(Long incidenteId);

    Optional<VotoComisario> findByIncidente_IdAndComisario_Id(Long incidenteId, Long comisarioId);

    List<VotoComisario> findByComisario_IdOrderByFechaDesc(Long comisarioId);

    @Modifying
    @Query(value = "DELETE FROM voto_comisario WHERE incidente_id IN (SELECT id FROM incidente WHERE carrera_id = ?1)", nativeQuery = true)
    void deleteByCarreraId(Long carreraId);
}
