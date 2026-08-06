package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Apelacion;
import org.example.lfmnacional.enums.EstadoApelacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {

    List<Apelacion> findByUsuario_Id(Long usuarioId);

    List<Apelacion> findBySancion_Id(Long sancionId);

    List<Apelacion> findByEstado(EstadoApelacion estado);
}
