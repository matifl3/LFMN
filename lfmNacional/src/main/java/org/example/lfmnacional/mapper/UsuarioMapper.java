package org.example.lfmnacional.mapper;

import org.example.lfmnacional.dto.usuario.UsuarioBasicoResponse;
import org.example.lfmnacional.dto.usuario.UsuarioResponse;
import org.example.lfmnacional.entity.Usuario;

/**
 * Unico lugar que arma las vistas de un {@link Usuario}. Es el agregado que mas
 * servicios devuelven (perfil, login, registro, admin) y tiene una regla de
 * privacidad: la vista publica no lleva email, GUID de Steam ni estado de la
 * password. El resto de los agregados se mapea en su propio servicio (ver §2.3
 * de docs/arquitectura-y-patrones.md).
 */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    /** Para el propio usuario y para ADMIN. */
    public static UsuarioResponse completo(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombrePiloto(),
                usuario.getFotoPerfil(),
                usuario.getGuidSteam(),
                usuario.getElo(),
                usuario.getSafetyRating(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                usuario.isPasswordEstablecida(),
                usuario.isHabilitado());
    }

    /** Perfil de piloto visto por otro: sin datos privados. */
    public static UsuarioResponse publico(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                null,
                usuario.getNombrePiloto(),
                usuario.getFotoPerfil(),
                null,
                usuario.getElo(),
                usuario.getSafetyRating(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                null,
                usuario.isHabilitado());
    }

    public static UsuarioBasicoResponse basico(Usuario usuario) {
        return new UsuarioBasicoResponse(
                usuario.getId(), usuario.getNombrePiloto(), usuario.getFotoPerfil(),
                usuario.getElo(), usuario.getSafetyRating());
    }
}
