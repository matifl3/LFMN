package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.repository.UsuarioRepository;

/**
 * ELO y Safety Rating hacen lo mismo sobre un numero distinto (Template Method):
 * mover el rating sin bajar de 0, guardar lo aplicado en
 * {@code Sancion.cambioAplicado} para revertir exactamente eso, y dejar una fila
 * en el historial. Las subclases solo dicen que numero y que historial.
 */
abstract class EfectoRating implements EfectoSancion {

    private final UsuarioRepository usuarioRepository;

    protected EfectoRating(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Mueve el rating del usuario y devuelve lo que se aplico de verdad. */
    protected abstract int ajustar(Usuario usuario, int pedido);

    protected abstract void registrarHistorial(Usuario usuario, int cambio, String motivo, Carrera carrera);

    protected abstract String motivoPorDefecto();

    @Override
    public void aplicar(Sancion sancion) {
        Usuario usuario = sancion.getUsuario();
        int aplicado = ajustar(usuario, sancion.getValor() != null ? sancion.getValor() : 0);
        sancion.setCambioAplicado(aplicado);
        usuarioRepository.save(usuario);
        registrarHistorial(usuario, aplicado,
                sancion.getMotivo() != null ? sancion.getMotivo() : motivoPorDefecto(),
                sancion.getCarrera());
    }

    @Override
    public void revertir(Sancion sancion) {
        Usuario usuario = sancion.getUsuario();
        int revertido = ajustar(usuario, -sancion.cambioARevertir());
        usuarioRepository.save(usuario);
        registrarHistorial(usuario, revertido,
                "Reversion de sancion " + sancion.getId() + ": "
                        + (sancion.getMotivo() != null ? sancion.getMotivo() : "sancion"),
                sancion.getCarrera());
    }
}
