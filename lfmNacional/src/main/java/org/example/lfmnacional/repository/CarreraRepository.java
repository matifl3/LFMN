package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    List<Carrera> findByCampeonato_IdAndEstado(Long campeonatoId, EstadoCarrera estado);

    List<Carrera> findByEstado(EstadoCarrera estado);

    List<Carrera> findByEstadoInAndFechaBefore(List<EstadoCarrera> estados, LocalDateTime fecha);

    List<Carrera> findByFechaAfterOrderByFechaAsc(LocalDateTime fecha);

    List<Carrera> findByFechaBeforeOrderByFechaDesc(LocalDateTime fecha);

    List<Carrera> findByCampeonato_IdOrderByFechaDesc(Long campeonatoId);

    Page<Carrera> findAll(Pageable pageable);
}
