package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.UsuarioRecompensa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRecompensaRepository extends JpaRepository<UsuarioRecompensa, Long> {

    Optional<UsuarioRecompensa> findByRecompensa_IdAndUsuario_Id(Long recompensaId, Long usuarioId);

    List<UsuarioRecompensa> findByUsuario_Id(Long usuarioId);

    List<UsuarioRecompensa> findByUsuario_IdAndReclamadaFalse(Long usuarioId);
}
