package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionProcesada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesionProcesadaRepository extends JpaRepository<SesionProcesada, Long> {

    boolean existsByClave(String clave);

    Optional<SesionProcesada> findByClave(String clave);

    List<SesionProcesada> findAllByOrderByFechaProcesamientoDesc();

    List<SesionProcesada> findByCarrera_IdOrderByFechaProcesamientoDesc(Long carreraId);

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);
}
