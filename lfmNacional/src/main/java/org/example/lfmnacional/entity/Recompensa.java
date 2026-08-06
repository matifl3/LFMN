package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.TipoRecompensa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recompensa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "logro_id", nullable = false)
    private Logro logro;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecompensa tipo;
}
