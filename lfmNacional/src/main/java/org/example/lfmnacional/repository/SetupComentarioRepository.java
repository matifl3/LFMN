package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SetupComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SetupComentarioRepository extends JpaRepository<SetupComentario, Long> {

    List<SetupComentario> findBySetup_IdOrderByFechaDesc(Long setupId);

    void deleteBySetup_Id(Long setupId);

    @Modifying
    @Query("delete from SetupComentario sc where sc.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);

    @Modifying
    @Query("delete from SetupComentario sc where sc.setup.autor.id = ?1")
    void deleteBySetupAutorId(Long autorId);
}
