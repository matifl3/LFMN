package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultado_carrera",
        uniqueConstraints = @UniqueConstraint(columnNames = {"carrera_id", "usuario_id"}),
        indexes = {
                @Index(name = "idx_rc_usuario", columnList = "usuario_id"),
                @Index(name = "idx_rc_usuario_finalizo", columnList = "usuario_id, finalizo"),
                @Index(name = "idx_rc_usuario_posicion", columnList = "usuario_id, posicion_final"),
                @Index(name = "idx_rc_carrera_vuelta_rapida", columnList = "carrera_id, vuelta_rapida")
        })
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

    /**
     * Posicion con la que se cargo el resultado, antes de cualquier sancion.
     * Las sanciones reclasifican siempre desde aca, asi revertir una no depende
     * del estado que dejo otra. Null en filas viejas: se completa con la
     * posicion actual la primera vez que se reclasifica la carrera.
     */
    @Column(name = "posicion_original")
    private Integer posicionOriginal;

    /**
     * Vueltas completadas segun la sesion de AC. Ordena antes que el tiempo:
     * el {@code TotalTime} de un doblado puede ser menor que el del ganador.
     * Null en cargas manuales y filas viejas: entonces solo cuenta el tiempo.
     */
    @Column(name = "vueltas_completadas")
    private Integer vueltasCompletadas;

    @Column(name = "tiempo_total")
    private Long tiempoTotal;

    @Column(name = "vuelta_rapida")
    private Long vueltaRapida;

    @Column(name = "modelo_auto")
    private String modeloAuto;

    @Column(name = "skin_auto")
    private String skinAuto;

    @Column(nullable = false)
    private boolean poles = false;

    @Column(nullable = false)
    private boolean finalizo = false;

    @Column(name = "elo_ganado")
    private Integer eloGanado;

    @Column(name = "sr_ganado")
    private Integer srGanado;
}
