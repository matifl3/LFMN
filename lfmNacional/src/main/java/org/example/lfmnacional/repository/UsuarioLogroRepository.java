package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.UsuarioLogro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioLogroRepository extends JpaRepository<UsuarioLogro, Long> {

    Optional<UsuarioLogro> findByLogro_IdAndUsuario_Id(Long logroId, Long usuarioId);

    List<UsuarioLogro> findByUsuario_IdOrderByLogro_Id(Long usuarioId);

    List<UsuarioLogro> findByUsuario_IdAndObtenidoTrue(Long usuarioId);

    void deleteByLogro_Id(Long logroId);

    @Modifying
    @Query("delete from UsuarioLogro u where u.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
