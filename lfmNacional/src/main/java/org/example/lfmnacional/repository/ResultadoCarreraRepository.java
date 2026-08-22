package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ResultadoCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResultadoCarreraRepository extends JpaRepository<ResultadoCarrera, Long> {

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    List<ResultadoCarrera> findByCarrera_IdOrderByPosicionFinalAsc(Long carreraId);

    Optional<ResultadoCarrera> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    List<ResultadoCarrera> findByUsuario_Id(Long usuarioId);

    long countByUsuario_Id(Long usuarioId);

    long countByUsuario_IdAndFinalizoTrue(Long usuarioId);

    long countByUsuario_IdAndPosicionFinal(Long usuarioId, Integer posicionFinal);

    long countByUsuario_IdAndPosicionFinalLessThanEqual(Long usuarioId, Integer posicionFinal);

    long countByUsuario_IdAndPolesTrue(Long usuarioId);

    @Query("select count(distinct r.carrera.id) from ResultadoCarrera r " +
            "where r.usuario.id = :usuarioId and r.vueltaRapida is not null " +
            "and r.vueltaRapida = (select min(r2.vueltaRapida) from ResultadoCarrera r2 " +
            "where r2.carrera.id = r.carrera.id and r2.vueltaRapida is not null)")
    long countVueltaRapidaByUsuario(@Param("usuarioId") Long usuarioId);
}
