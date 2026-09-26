package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SesionClasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SesionClasificacionRepository extends JpaRepository<SesionClasificacion, Long> {

    /**
     * Filtra por lo que el visor puede ver: todo lo publico, mas lo privado de los
     * campeonato que administra o de los que es miembro. El ADMIN global y el
     * COMISARIO no se filtran (se consulta con spectatorId = null).
     */
    @Query("""
            select s from SesionClasificacion s
            where (:spectatorId is null
                   or s.carrera.campeonato.visibilidad <> org.example.lfmnacional.enums.VisibilidadCampeonato.PRIVADO
                   or s.carrera.campeonato.admin.id = :spectatorId
                   or exists (select m from CampeonatoMiembro m
                              where m.campeonato = s.carrera.campeonato
                                and m.usuario.id = :spectatorId))
            """)
    List<SesionClasificacion> findVisiblesPara(@Param("spectatorId") Long spectatorId);

    @Query("""
            select s from SesionClasificacion s
            where s.usuario.id = :usuarioId
              and (:spectatorId is null
                   or s.carrera.campeonato.visibilidad <> org.example.lfmnacional.enums.VisibilidadCampeonato.PRIVADO
                   or s.carrera.campeonato.admin.id = :spectatorId
                   or exists (select m from CampeonatoMiembro m
                              where m.campeonato = s.carrera.campeonato
                                and m.usuario.id = :spectatorId))
            order by s.tiempo asc
            """)
    List<SesionClasificacion> findVisiblesPorUsuario(@Param("usuarioId") Long usuarioId,
                                                     @Param("spectatorId") Long spectatorId);

    List<SesionClasificacion> findByCarrera_IdOrderByTiempoAsc(Long carreraId);

    List<SesionClasificacion> findByUsuario_IdOrderByTiempoAsc(Long usuarioId);

    Optional<SesionClasificacion> findByCarrera_IdAndUsuario_Id(Long carreraId, Long usuarioId);

    boolean existsByCarrera_Id(Long carreraId);

    void deleteByCarrera_Id(Long carreraId);

    @Modifying
    @Query("delete from SesionClasificacion s where s.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
