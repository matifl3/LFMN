package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.enums.OrigenSancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SancionRepository extends JpaRepository<Sancion, Long> {

    List<Sancion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Sancion> findByCarrera_Id(Long carreraId);

    List<Sancion> findByResolucion_Id(Long resolucionId);

    Optional<Sancion> findByOrigenAndIdExterno(OrigenSancion origen, String idExterno);

    boolean existsByOrigenAndIdExterno(OrigenSancion origen, String idExterno);

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from Sancion s where s.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Modifying
    @Query("delete from Sancion s where s.resolucion.incidente.reportante.id = ?1")
    void deleteByResolucionIncidenteReportanteId(Long reportanteId);
}
