package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.enums.RolPilotoIncidente;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "incidente_piloto",
        uniqueConstraints = @UniqueConstraint(columnNames = {"incidente_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentePiloto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolPilotoIncidente rol;
}
