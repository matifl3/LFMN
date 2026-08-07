package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionProcesada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionProcesadaRepository extends JpaRepository<SesionProcesada, Long> {

    boolean existsByNombreArchivo(String nombreArchivo);
}
