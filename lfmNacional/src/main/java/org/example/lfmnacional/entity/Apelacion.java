package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.EstadoApelacion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "apelacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sancion_id", nullable = false)
    private Sancion sancion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoApelacion estado;

    @Column(name = "respuesta_admin", length = 1000)
    private String respuestaAdmin;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoApelacion.PENDIENTE;
        }
    }
}
