package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {

    List<Incidente> findByEstado(EstadoIncidente estado);

    List<Incidente> findByCarrera_Id(Long carreraId);

    List<Incidente> findByReportante_Id(Long reportanteId);

    List<Incidente> findByEstadoOrderByEstadoAsc(EstadoIncidente estado);

    boolean existsByCarrera_Id(Long carreraId);

    long countByEstado(EstadoIncidente estado);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from Incidente i where i.reportante.id = ?1")
    void deleteByReportante_Id(Long reportanteId);
}
