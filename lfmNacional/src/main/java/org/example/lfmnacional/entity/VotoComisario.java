package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.DecisionComisario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voto_comisario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"incidente_id", "comisario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoComisario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comisario_id", nullable = false)
    private Usuario comisario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionComisario decision;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
