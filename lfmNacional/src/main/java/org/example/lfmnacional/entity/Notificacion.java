package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.TipoNotificacion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion",
        indexes = @Index(name = "idx_notif_usuario_leida_fecha", columnList = "usuario_id, leida, fecha"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class    Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String link;

    @PrePersist
    public void prePersist() {
        if (leida == null) {
            leida = false;
        }
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
