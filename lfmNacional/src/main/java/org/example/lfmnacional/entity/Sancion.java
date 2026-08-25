package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.TipoSancion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sancion",
        indexes = {
                @Index(name = "idx_sancion_usuario_fecha", columnList = "usuario_id, fecha"),
                @Index(name = "idx_sancion_origen_id_externo", columnList = "origen, id_externo")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id")
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolucion_id")
    private ResolucionIncidente resolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSancion tipo;

    private Integer valor;

    @Column(length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenSancion origen;

    @Column(name = "id_externo", length = 100)
    private String idExterno;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "efectos_aplicados", nullable = false)
    private Boolean efectosAplicados = true;

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (efectosAplicados == null) {
            efectosAplicados = true;
        }
    }
}
