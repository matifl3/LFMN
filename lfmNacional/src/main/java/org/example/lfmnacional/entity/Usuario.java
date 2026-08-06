package org.example.lfmnacional.entity;

import org.example.lfmnacional.enums.Rol;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nombre_piloto", length = 100)
    private String nombrePiloto;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "guid_steam", unique = true, length = 40)
    private String guidSteam;

    @Column(nullable = false)
    private Integer elo;

    @Column(name = "safety_rating", nullable = false)
    private Integer safetyRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "usuario")
    private List<Sancion> sanciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<EloSancion> eloSanciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<SafetyRatingSancion> safetyRatingSanciones = new ArrayList<>();

    @OneToMany(mappedBy = "autor")
    private List<Setup> setups = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Notificacion> notificaciones = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (rol == null) {
            rol = Rol.USUARIO;
        }
        if (elo == null) {
            elo = 1500;
        }
        if (safetyRating == null) {
            safetyRating = 100;
        }
    }
}
