package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {

    List<Recompensa> findByLogro_Id(Long logroId);
}
