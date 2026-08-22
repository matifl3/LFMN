package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.VueltaCarrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VueltaRepository extends JpaRepository<VueltaCarrera, Long> {

    List<VueltaCarrera> findByCarrera_IdOrderByUsuario_IdAscNumeroVueltaAsc(Long carreraId);

    List<VueltaCarrera> findByCarrera_IdAndUsuario_IdOrderByNumeroVueltaAsc(Long carreraId, Long usuarioId);

    boolean existsByCarrera_IdAndTipo(Long carreraId, String tipo);

    void deleteByCarrera_IdAndTipo(Long carreraId, String tipo);

    void deleteByCarrera_Id(Long carreraId);
}
