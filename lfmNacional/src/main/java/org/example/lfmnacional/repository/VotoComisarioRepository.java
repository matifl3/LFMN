package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.VotoComisario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VotoComisarioRepository extends JpaRepository<VotoComisario, Long> {

    List<VotoComisario> findByIncidente_Id(Long incidenteId);

    Optional<VotoComisario> findByIncidente_IdAndComisario_Id(Long incidenteId, Long comisarioId);

    List<VotoComisario> findByComisario_IdOrderByFechaDesc(Long comisarioId);
}
