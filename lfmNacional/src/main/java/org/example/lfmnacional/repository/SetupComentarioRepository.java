package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.SetupComentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetupComentarioRepository extends JpaRepository<SetupComentario, Long> {

    List<SetupComentario> findBySetup_IdOrderByFechaDesc(Long setupId);

    void deleteBySetup_Id(Long setupId);
}
