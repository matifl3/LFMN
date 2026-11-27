package org.example.lfmnacional.entity;

import org.example.lfmnacional.exception.BusinessException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    /** Largo minimo de una password elegida por el usuario (registro, cambio, reset). */
    public static final int MIN_PASSWORD = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "password_establecida", nullable = false, columnDefinition = "boolean default true")
    private boolean passwordEstablecida = true;

    @Builder.Default
    @Column(name = "token_version", nullable = false, columnDefinition = "int default 0")
    private Integer tokenVersion = 0;

    @Size(max = 100)
    @Column(name = "nombre_piloto", unique = true, length = 100)
    private String nombrePiloto;

    @Size(max = 255)
    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Pattern(regexp = "\\d{17}")
    @Size(max = 40)
    @Column(name = "guid_steam", unique = true, length = 40)
    private String guidSteam;

    @Min(0)
    @Column(nullable = false)
    private Integer elo;

    @Min(0)
    @Column(name = "safety_rating", nullable = false)
    private Integer safetyRating;

    @Version
    private Long version;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean habilitado = true;

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

    /** Version de tokens vigente (null en filas viejas = 0). Va en el claim "tv" del JWT. */
    public int getVersionTokens() {
        return tokenVersion != null ? tokenVersion : 0;
    }

    /** Invalida todo JWT y link de recuperacion emitido hasta ahora. */
    public void invalidarTokens() {
        tokenVersion = getVersionTokens() + 1;
    }

    /**
     * Reemplaza la password por {@code hash} (ya codificado), la marca como propia
     * y cierra las sesiones abiertas. Unico punto que cambia la password.
     */
    public void establecerPassword(String hash) {
        password = hash;
        passwordEstablecida = true;
        invalidarTokens();
    }

    /**
     * Mueve el Elo en {@code pedido} sin bajar de 0 (igual que al cargar
     * resultados) y devuelve lo que se aplico de verdad, que es lo que despues
     * se revierte.
     */
    public int ajustarElo(int pedido) {
        int aplicado = cambioSinBajarDeCero(elo, pedido);
        elo = elo + aplicado;
        return aplicado;
    }

    /** Como {@link #ajustarElo}, para el Safety Rating. */
    public int ajustarSafetyRating(int pedido) {
        int aplicado = cambioSinBajarDeCero(safetyRating, pedido);
        safetyRating = safetyRating + aplicado;
        return aplicado;
    }

    private static int cambioSinBajarDeCero(int actual, int pedido) {
        return Math.max(0, actual + pedido) - actual;
    }

    /** Valida una password nueva en texto plano antes de codificarla. */
    public static void validarPasswordNueva(String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.length() < MIN_PASSWORD) {
            throw new BusinessException(
                    "La nueva contrasena debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
    }

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
