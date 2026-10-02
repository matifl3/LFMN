package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_procesada",
        uniqueConstraints = @UniqueConstraint(columnNames = "clave"))
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

    // Identidad del JSON calculada sobre su contenido (carrera + track + tipo +
    // cantidad de autos/resultados/vueltas/eventos). Es la clave de idempotencia
    // real: el nombre de archivo puede cambiar entre reintentos del cliente.
    @Column(name = "clave", nullable = false, unique = true, length = 40)
    private String clave;

    // Trazabilidad del archivo original. Null cuando la ingesta es por HTTP
    // (POST /api/sesiones/importar), donde el cliente no envia nombre.
    @Column(name = "nombre_archivo")
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
