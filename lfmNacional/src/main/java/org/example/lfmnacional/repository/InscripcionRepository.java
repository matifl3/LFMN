package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByCarrera_Id(Long carreraId);

    List<Inscripcion> findByUsuario_Id(Long usuarioId);

    Optional<Inscripcion> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    List<Inscripcion> findByCarrera_IdAndEstado(Long carreraId, EstadoInscripcion estado);

    long countByCarrera_IdAndEstado(Long carreraId, EstadoInscripcion estado);

    boolean existsByCarrera_Id(Long carreraId);
}
