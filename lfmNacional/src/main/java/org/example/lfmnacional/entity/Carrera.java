package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.EstadoCarrera;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrera")
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

    @OneToMany(mappedBy = "carrera")
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "carrera")
    private List<ResultadoCarrera> resultados = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archivo_id")
    private ArchivoCarrera archivo;

    @OneToMany(mappedBy = "carrera")
    private List<Incidente> incidentes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoCarrera.PROGRAMADA;
        }
    }
}
