package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.TipoSancion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Para ELO y SAFETY_RATING: lo que se le movio de verdad al piloto. Puede ser
     * menos que {@code valor} porque el rating no baja de 0; revertir devuelve
     * esto y no {@code valor}, si no el piloto ganaria rating al revertir.
     * Null en sanciones viejas o de otros tipos: se revierte {@code valor}.
     */
    @Column(name = "cambio_aplicado")
    private Integer cambioAplicado;

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

    @OneToMany(mappedBy = "sancion", cascade = CascadeType.REMOVE)
    private List<Apelacion> apelaciones = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (efectosAplicados == null) {
            efectosAplicados = true;
        }
    }

    /** Lo que se aplico de verdad; en sanciones viejas sin ese dato, el valor pedido. */
    public int cambioARevertir() {
        if (cambioAplicado != null) {
            return cambioAplicado;
        }
        return valor != null ? valor : 0;
    }
}
