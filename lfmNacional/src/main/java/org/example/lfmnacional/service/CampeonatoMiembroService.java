package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.MiembroCampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.MiembroCampeonatoResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoMiembro;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.EntityMapper;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Roster de pilotos de un campeonato. La unica forma de entrar a un campeonato
 * privado es que el administrador lo sume desde aca.
 */
@Service
@RequiredArgsConstructor
public class CampeonatoMiembroService {

    private final CampeonatoMiembroRepository miembroRepository;
    private final NotificacionRepository notificacionRepository;
    private final CampeonatoService campeonatoService;
    private final CampeonatoAccesoService accesoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MiembroCampeonatoResponse> listar(Long campeonatoId, Usuario usuario) {
        Campeonato campeonato = campeonatoService.getEntity(campeonatoId);
        accesoService.exigirVeContenido(usuario, campeonato);
        return listarMiembros(campeonatoId);
    }

    @Transactional
    public MiembroCampeonatoResponse agregar(Long campeonatoId, MiembroCampeonatoRequest request, Usuario actual) {
        Campeonato campeonato = campeonatoService.getEntity(campeonatoId);
        accesoService.exigirAdministra(actual, campeonato);
        Usuario piloto = buscarPiloto(request);
        if (miembroRepository.existsByCampeonato_IdAndUsuario_Id(campeonatoId, piloto.getId())) {
            throw new BusinessException("El piloto " + piloto.getNombrePiloto()
                    + " ya forma parte del campeonato '" + campeonato.getNombre() + "'");
        }
        CampeonatoMiembro alta = miembroRepository.save(CampeonatoMiembro.builder()
                .campeonato(campeonato)
                .usuario(piloto)
                .build());
        notificarAlPiloto(alta);
        return toResponse(alta);
    }

    @Transactional
    public void quitar(Long campeonatoId, Long usuarioId, Usuario actual) {
        Campeonato campeonato = campeonatoService.getEntity(campeonatoId);
        accesoService.exigirAdministra(actual, campeonato);
        if (!miembroRepository.existsByCampeonato_IdAndUsuario_Id(campeonatoId, usuarioId)) {
            throw new ResourceNotFoundException("El piloto no forma parte de este campeonato");
        }
        miembroRepository.deleteByCampeonato_IdAndUsuario_Id(campeonatoId, usuarioId);
    }

    private List<MiembroCampeonatoResponse> listarMiembros(Long campeonatoId) {
        return miembroRepository.findByCampeonato_IdOrderByFechaAltaAsc(campeonatoId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Usuario buscarPiloto(MiembroCampeonatoRequest request) {
        boolean porEmail = request.email() != null && !request.email().isBlank();
        boolean porNombre = request.nombrePiloto() != null && !request.nombrePiloto().isBlank();
        if (!porEmail && !porNombre) {
            throw new BusinessException("Indicá el email o el nombre de piloto para sumar al campeonato");
        }
        Usuario piloto = porEmail
                ? usuarioRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null)
                : usuarioRepository.findByNombrePiloto(request.nombrePiloto().trim()).orElse(null);
        if (piloto == null) {
            throw new ResourceNotFoundException("No existe un usuario registrado con "
                    + (porEmail ? "ese email" : "ese nombre de piloto"));
        }
        if (!piloto.isHabilitado()) {
            throw new BusinessException("El piloto " + piloto.getNombrePiloto() + " esta deshabilitado");
        }
        return piloto;
    }

    private void notificarAlPiloto(CampeonatoMiembro alta) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(alta.getUsuario())
                .tipo(TipoNotificacion.CAMPEONATO)
                .mensaje("Te agregaron como piloto al campeonato \""
                        + alta.getCampeonato().getNombre() + "\".")
                .leida(false)
                .link("/campeonato")
                .build());
    }

    private MiembroCampeonatoResponse toResponse(CampeonatoMiembro alta) {
        var u = EntityMapper.resolveUsuarioBasico(alta.getUsuario());
        return new MiembroCampeonatoResponse(
                u.id(),
                u.nombrePiloto(),
                u.fotoPerfil(),
                alta.getUsuario().getElo(),
                alta.getUsuario().getSafetyRating(),
                alta.getFechaAlta());
    }
}
