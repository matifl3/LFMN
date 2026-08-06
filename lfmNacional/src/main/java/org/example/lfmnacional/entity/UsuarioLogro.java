package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_logro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"logro_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "logro_id", nullable = false)
    private Logro logro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Integer progreso;

    @Column(nullable = false)
    private Boolean obtenido;

    @Column(name = "fecha_obtencion")
    private LocalDateTime fechaObtencion;

    @PrePersist
    public void prePersist() {
        if (progreso == null) {
            progreso = 0;
        }
        if (obtenido == null) {
            obtenido = false;
        }
    }
}
