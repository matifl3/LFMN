package org.example.lfmnacional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "elo_minimo")
    private Integer eloMinimo;

    @Column(name = "elo_maximo")
    private Integer eloMaximo;

    @Column(name = "setup_abierto")
    private Boolean setupAbierto;

    @Column(name = "setup_fijo")
    private Boolean setupFijo;

    @OneToMany(mappedBy = "categoria")
    private List<Campeonato> campeonatos = new ArrayList<>();
}
