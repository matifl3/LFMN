package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Notificacion> findByUsuario_IdAndLeidaFalseOrderByFechaDesc(Long usuarioId);

    List<Notificacion> findByUsuario_IdAndTipo(Long usuarioId, TipoNotificacion tipo);

    boolean existsByTipoAndLink(TipoNotificacion tipo, String link);

    long countByUsuario_IdAndLeidaFalse(Long usuarioId);

    @Modifying
    @Query("delete from Notificacion n where n.usuario.id = ?1")
    void deleteByUsuario_Id(Long usuarioId);
}
