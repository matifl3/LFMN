package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.TipoArchivo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "archivo_carrera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String ruta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoArchivo tipo;
}
