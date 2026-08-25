package org.example.lfmnacional.mapper;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;

public final class EntityMapper {

    private EntityMapper() {}

    public record CarreraInfo(Long id, String nombre, String categoriaNombre) {}
    public record UserInfo(Long id, String nombrePiloto, String fotoPerfil) {}

    public static String resolveCategoriaNombre(Carrera carrera) {
        if (carrera == null) return null;
        if (carrera.getCampeonato() == null) return null;
        if (carrera.getCampeonato().getCategoria() == null) return null;
        return carrera.getCampeonato().getCategoria().getNombre();
    }

    public static CarreraInfo resolveCarreraInfo(Carrera carrera) {
        if (carrera == null) return null;
        return new CarreraInfo(
                carrera.getId(),
                carrera.getNombre(),
                resolveCategoriaNombre(carrera));
    }

    public static UserInfo resolveUsuarioBasico(Usuario usuario) {
        if (usuario == null) return null;
        return new UserInfo(
                usuario.getId(),
                usuario.getNombrePiloto(),
                usuario.getFotoPerfil());
    }
}
