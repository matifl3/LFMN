package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.enums.VisibilidadCampeonato;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "campeonato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String temporada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCampeonato estado;

    @Column(name = "sistema_puntos", length = 100)
    private String sistemaPuntos; //descripcion de como es el sistema de puntos en ese campeonato

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilidadCampeonato visibilidad;

    /**
     * Administrador dueno del campeonato. Solo se usa cuando la visibilidad es
     * PRIVADO. Un admin de campeonato puede administrar varios. Es nullable
     * porque los explicitos de la liga son los que los crea el ADMIN global.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Usuario admin;

    @OneToMany(mappedBy = "campeonato")
    private List<CampeonatoPosicion> posiciones = new ArrayList<>();

    @OneToMany(mappedBy = "campeonato")
    private List<Carrera> carreras = new ArrayList<>();

    @OneToMany(mappedBy = "campeonato")
    private List<CampeonatoMiembro> miembros = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoCampeonato.ACTIVO;
        }
        if (visibilidad == null) {
            visibilidad = VisibilidadCampeonato.PUBLICO;
        }
    }
}
