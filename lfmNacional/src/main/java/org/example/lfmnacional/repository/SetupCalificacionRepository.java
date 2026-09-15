package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SetupCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SetupCalificacionRepository extends JpaRepository<SetupCalificacion, Long> {

    Optional<SetupCalificacion> findBySetup_IdAndUsuario_Id(Long setupId, Long usuarioId);

    List<SetupCalificacion> findBySetup_Id(Long setupId);

    void deleteBySetup_Id(Long setupId);

    @Modifying
    @Query("delete from SetupCalificacion sc where sc.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Modifying
    @Query("delete from SetupCalificacion sc where sc.setup.autor.id = ?1")
    void deleteBySetupAutorId(Long autorId);
}
