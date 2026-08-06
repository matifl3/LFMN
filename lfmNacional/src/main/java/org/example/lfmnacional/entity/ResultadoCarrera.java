package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultado_carrera",
        uniqueConstraints = @UniqueConstraint(columnNames = {"carrera_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "posicion_final")
    private Integer posicionFinal;

    @Column(name = "tiempo_total")
    private Long tiempoTotal;

    @Column(name = "vuelta_rapida")
    private Long vueltaRapida;

    private Boolean poles;

    private Boolean finalizo;

    @Column(name = "elo_ganado")
    private Integer eloGanado;

    @Column(name = "sr_ganado")
    private Integer srGanado;
}
