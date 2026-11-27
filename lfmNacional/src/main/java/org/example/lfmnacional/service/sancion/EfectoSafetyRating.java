package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class EfectoSafetyRating extends EfectoRating {

    private final SafetyRatingSancionRepository safetyRatingSancionRepository;

    public EfectoSafetyRating(UsuarioRepository usuarioRepository,
                              SafetyRatingSancionRepository safetyRatingSancionRepository) {
        super(usuarioRepository);
        this.safetyRatingSancionRepository = safetyRatingSancionRepository;
    }

    @Override
    public TipoSancion tipo() {
        return TipoSancion.SAFETY_RATING;
    }

    @Override
    protected int ajustar(Usuario usuario, int pedido) {
        return usuario.ajustarSafetyRating(pedido);
    }

    @Override
    protected void registrarHistorial(Usuario usuario, int cambio, String motivo, Carrera carrera) {
        safetyRatingSancionRepository.save(SafetyRatingSancion.builder()
                .usuario(usuario).cambio(cambio).motivo(motivo).carrera(carrera).build());
    }

    @Override
    protected String motivoPorDefecto() {
        return "Ajuste manual de Safety Rating";
    }
}
