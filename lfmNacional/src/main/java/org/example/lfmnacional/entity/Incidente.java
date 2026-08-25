package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.EstadoIncidente;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidente",
        indexes = @Index(name = "idx_incidente_estado_carrera", columnList = "estado, carrera_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    private Integer vuelta;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoIncidente estado;

    @OneToMany(mappedBy = "incidente", cascade = CascadeType.REMOVE)
    private List<IncidentePiloto> pilotos = new ArrayList<>();

    @OneToMany(mappedBy = "incidente", cascade = CascadeType.REMOVE)
    private List<VotoComisario> votos = new ArrayList<>();

    @OneToOne(mappedBy = "incidente", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResolucionIncidente resolucion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoIncidente.PENDIENTE;
        }
    }
}
