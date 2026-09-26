package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.ResultadoCarrera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResultadoCarreraRepository extends JpaRepository<ResultadoCarrera, Long> {

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from ResultadoCarrera r where r.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    List<ResultadoCarrera> findByCarrera_IdOrderByPosicionFinalAsc(Long carreraId);

    Optional<ResultadoCarrera> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    List<ResultadoCarrera> findByUsuario_Id(Long usuarioId);

    Page<ResultadoCarrera> findByUsuario_Id(Long usuarioId, Pageable pageable);

    /**
     * Historial paginado sin filtrar las carreras privadas que el espectador no puede
     * ver. Con {@code spectatorId = null} (ADMIN global o COMISARIO) no filtra nada.
     */
    @Query(value = """
            select r from ResultadoCarrera r
            where r.usuario.id = :usuarioId
              and (:spectatorId is null
                   or r.carrera.campeonato.visibilidad <> org.example.lfmnacional.enums.VisibilidadCampeonato.PRIVADO
                   or r.carrera.campeonato.admin.id = :spectatorId
                   or exists (select m from CampeonatoMiembro m
                              where m.campeonato = r.carrera.campeonato
                                and m.usuario.id = :spectatorId))
            """,
            countQuery = """
            select count(r) from ResultadoCarrera r
            where r.usuario.id = :usuarioId
              and (:spectatorId is null
                   or r.carrera.campeonato.visibilidad <> org.example.lfmnacional.enums.VisibilidadCampeonato.PRIVADO
                   or r.carrera.campeonato.admin.id = :spectatorId
                   or exists (select m from CampeonatoMiembro m
                              where m.campeonato = r.carrera.campeonato
                                and m.usuario.id = :spectatorId))
            """)
    Page<ResultadoCarrera> findVisiblesPorUsuario(@Param("usuarioId") Long usuarioId,
                                                  @Param("spectatorId") Long spectatorId,
                                                  Pageable pageable);

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

    @Query("select count(distinct r.usuario.id) from ResultadoCarrera r")
    long countUsuariosConResultados();
}
