package org.example.lfmnacional.repository;

import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<Notificacion> findByUsuario_IdAndLeidaFalseOrderByFechaDesc(Long usuarioId);

    List<Notificacion> findByUsuario_IdAndTipo(Long usuarioId, TipoNotificacion tipo);

    long countByUsuario_IdAndLeidaFalse(Long usuarioId);
}
