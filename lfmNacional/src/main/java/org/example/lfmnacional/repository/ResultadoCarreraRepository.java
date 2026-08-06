package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ResultadoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultadoCarreraRepository extends JpaRepository<ResultadoCarrera, Long> {

    List<ResultadoCarrera> findByCarrera_IdOrderByPosicionFinalAsc(Long carreraId);

    Optional<ResultadoCarrera> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    List<ResultadoCarrera> findByUsuario_Id(Long usuarioId);

    long countByUsuario_IdAndFinalizoTrue(Long usuarioId);

    long countByUsuario_IdAndPosicionFinal(Long usuarioId, Integer posicionFinal);

    long countByUsuario_IdAndPosicionFinalLessThanEqual(Long usuarioId, Integer posicionFinal);

    long countByUsuario_IdAndPolesTrue(Long usuarioId);
}
