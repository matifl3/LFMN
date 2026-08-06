package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String circuito;

    @Column(nullable = false, length = 100)
    private String vehiculo;

    @Column(length = 255)
    private String archivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "promedio_calificacion")
    private Double promedioCalificacion;

    @OneToMany(mappedBy = "setup")
    private List<SetupCalificacion> calificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "setup")
    private List<SetupComentario> comentarios = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
    }
}
