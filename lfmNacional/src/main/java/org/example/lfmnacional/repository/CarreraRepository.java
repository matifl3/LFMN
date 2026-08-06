package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    List<Carrera> findByCategoria_IdAndEstado(Long categoriaId, EstadoCarrera estado);

    List<Carrera> findByEstado(EstadoCarrera estado);

    List<Carrera> findByFechaAfterOrderByFechaAsc(LocalDateTime fecha);

    List<Carrera> findByFechaBeforeOrderByFechaDesc(LocalDateTime fecha);

    List<Carrera> findByCategoria_IdOrderByFechaDesc(Long categoriaId);
}
