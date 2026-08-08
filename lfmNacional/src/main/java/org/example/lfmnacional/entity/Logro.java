package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.TipoCondicionLogro;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_condicion", nullable = false)
    private TipoCondicionLogro tipoCondicion;

    @Column(name = "valor_condicion", nullable = false)
    private Integer valorCondicion;

    @Column(length = 255)
    private String icono;

    @OneToMany(mappedBy = "logro", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recompensa> recompensas = new ArrayList<>();
}
