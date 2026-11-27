package org.example.lfmnacional.service.sancion;

import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class EfectoElo extends EfectoRating {

    private final EloSancionRepository eloSancionRepository;

    public EfectoElo(UsuarioRepository usuarioRepository, EloSancionRepository eloSancionRepository) {
        super(usuarioRepository);
        this.eloSancionRepository = eloSancionRepository;
    }

    @Override
    public TipoSancion tipo() {
        return TipoSancion.ELO;
    }

    @Override
    protected int ajustar(Usuario usuario, int pedido) {
        return usuario.ajustarElo(pedido);
    }

    @Override
    protected void registrarHistorial(Usuario usuario, int cambio, String motivo, Carrera carrera) {
        eloSancionRepository.save(EloSancion.builder()
                .usuario(usuario).cambio(cambio).motivo(motivo).carrera(carrera).build());
    }

    @Override
    protected String motivoPorDefecto() {
        return "Ajuste manual de Elo";
    }
}
