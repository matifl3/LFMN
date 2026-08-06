package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.IncidentePiloto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentePilotoRepository extends JpaRepository<IncidentePiloto, Long> {

    List<IncidentePiloto> findByIncidente_Id(Long incidenteId);

    List<IncidentePiloto> findByUsuario_Id(Long usuarioId);
}
