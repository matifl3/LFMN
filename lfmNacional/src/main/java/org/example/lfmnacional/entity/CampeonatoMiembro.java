package org.example.lfmnacional.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Piloto habilitado por el administrador de un campeonato para participar de el.
 * La presencia de la fila es lo que habilita la inscripcion a las carreras del
 * campeonato cuando la visibilidad es PRIVADO.
 */
@Entity
@Table(name = "campeonato_miembro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campeonato_id", "usuario_id"}),
        indexes = @Index(name = "idx_cmiembro_usuario", columnList = "usuario_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampeonatoMiembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    public void prePersist() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}
