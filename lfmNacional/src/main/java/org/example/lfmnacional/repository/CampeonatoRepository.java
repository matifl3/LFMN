package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.enums.VisibilidadCampeonato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {

    List<Campeonato> findByCategoria_Id(Long categoriaId);

    List<Campeonato> findByEstado(org.example.lfmnacional.enums.EstadoCampeonato estado);

    List<Campeonato> findByVisibilidad(VisibilidadCampeonato visibilidad);

    List<Campeonato> findByAdmin_IdOrderByIdDesc(Long adminId);

    @Modifying
    @Query("update Campeonato c set c.admin = null where c.admin.id = ?1")
    void desvincularAdmin(Long usuarioId);
}
