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
    //
    // IMPORTANTE: va nullable a proposito, no por descuido. En prod corre
    // ddl-auto=update con Flyway desactivado, asi que al agregar la columna la
    // genera Hibernate como "ADD COLUMN clave varchar(40) NOT NULL". MySQL
    // rechaza eso con ERROR 1364 si la tabla ya tiene filas, tira la excepcion
    // durante el arranque y el contenedor sale con status 1: la API entera se
    // caiga por una migracion de esquema.
    //
    // Nullable no rompe la unicidad: MySQL admite varios NULL en un indice
    // UNIQUE, asi que las filas viejas conviven con el constraint. El
    // invariante de "siempre hay clave" lo garantiza la aplicacion, que siempre
    // la calcula antes de insertar (IdempotenciaSesionService).
    //
    // Para volver a poner NOT NULL hay que primero backfillear las filas viejas
    // (ver docs/migracion_clave_sesion.sql) y recien despues cambiar el
    // nullable=false. Al reves, se repite el mismo crash.
    @Column(name = "clave", unique = true, length = 40)
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
