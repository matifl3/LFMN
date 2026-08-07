package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_procesada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @Column(name = "nombre_archivo", nullable = false, unique = true)
    private String nombreArchivo;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "fecha_procesamiento", nullable = false)
    private LocalDateTime fechaProcesamiento;

    @PrePersist
    public void prePersist() {
        if (fechaProcesamiento == null) {
            fechaProcesamiento = LocalDateTime.now();
        }
    }
}
