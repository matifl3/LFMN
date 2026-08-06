package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ResolucionIncidente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResolucionIncidenteRepository extends JpaRepository<ResolucionIncidente, Long> {

    Optional<ResolucionIncidente> findByIncidente_Id(Long incidenteId);

    java.util.List<ResolucionIncidente> findByComisario_Id(Long comisarioId);
}
