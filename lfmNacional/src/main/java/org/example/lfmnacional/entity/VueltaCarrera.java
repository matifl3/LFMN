package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vuelta_carrera",
        uniqueConstraints = @UniqueConstraint(columnNames = {"carrera_id", "usuario_id", "numero_vuelta", "tipo"}),
        indexes = {
                @Index(name = "idx_vc_carrera_usuario", columnList = "carrera_id, usuario_id"),
                @Index(name = "idx_vc_carrera_tipo", columnList = "carrera_id, tipo")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VueltaCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_vuelta", nullable = false)
    private Integer numeroVuelta;

    @Column(name = "tiempo_ms", nullable = false)
    private Long tiempoMs;

    @Column(name = "sector1")
    private Long sector1;

    @Column(name = "sector2")
    private Long sector2;

    @Column(name = "sector3")
    private Long sector3;

    private Integer cortes;

    private String neumatico;

    @Column(nullable = false)
    private String tipo;
}
