package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByCarrera_Id(Long carreraId);

    List<Inscripcion> findByUsuario_Id(Long usuarioId);

    Optional<Inscripcion> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    List<Inscripcion> findByCarrera_IdAndEstado(Long carreraId, EstadoInscripcion estado);

    long countByCarrera_IdAndEstado(Long carreraId, EstadoInscripcion estado);

    boolean existsByCarrera_Id(Long carreraId);

    boolean existsByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from Inscripcion i where i.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Query("select i.carrera.id, count(i) from Inscripcion i where i.estado = 'INSCRIPTO' group by i.carrera.id")
    List<Object[]> countInscriptosPorCarreraRaw();

/**
     * Inscripciones de todas las carreras de un campeonato. Sirve para contrastar
     * cuantos se anotaron contra cuantos efectivamente participaron, que es el
     * dato operativo que le importa al organizador.
     */
    List<Inscripcion> findByCarrera_Campeonato_Id(Long campeonatoId);
}
