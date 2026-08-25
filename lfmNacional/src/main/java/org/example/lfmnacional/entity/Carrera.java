package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.EstadoCarrera;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrera",
        indexes = {
                @Index(name = "idx_carrera_estado_fecha", columnList = "estado, fecha"),
                @Index(name = "idx_carrera_campeonato_fecha", columnList = "campeonato_id, fecha")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 100)
    private String circuito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarrera estado;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(length = 150)
    private String servidor;

    @Column(name = "contrasena_servidor", length = 100)
    private String contrasenaServidor;

    @Column(length = 500)
    private String linkPista;

    @Column(length = 500)
    private String linkAuto;

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<ResultadoCarrera> resultados = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archivo_id")
    private ArchivoCarrera archivo;

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<Incidente> incidentes = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<Sancion> sanciones = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<EloSancion> eloSanciones = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<SafetyRatingSancion> safetyRatingSanciones = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<VueltaCarrera> vueltas = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<SesionClasificacion> sesionesClasificacion = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", cascade = CascadeType.REMOVE)
    private List<SesionProcesada> sesionesProcesadas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoCarrera.PROGRAMADA;
        }
    }
}
