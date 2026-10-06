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

    @Modifying
    @Query("delete from IncidentePiloto ip where ip.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Modifying
    @Query("delete from IncidentePiloto ip where ip.incidente.reportante.id = ?1")
    void deleteByIncidenteReportanteId(Long reportanteId);

    /**
     * Pilotos involucrados en los incidentes de todas las carreras de un
     * campeonato. Trae el rol (CAUSANTE / AFECTADO) para poder separar los
     * incidentes que el piloto provoco de los que le pasaron.
     */
    List<IncidentePiloto> findByIncidente_Carrera_Campeonato_Id(Long campeonatoId);
}
