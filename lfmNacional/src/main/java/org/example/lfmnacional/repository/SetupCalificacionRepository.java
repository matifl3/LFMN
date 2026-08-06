package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SetupCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SetupCalificacionRepository extends JpaRepository<SetupCalificacion, Long> {

    Optional<SetupCalificacion> findBySetup_IdAndUsuario_Id(Long setupId, Long usuarioId);

    List<SetupCalificacion> findBySetup_Id(Long setupId);
}
