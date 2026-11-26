package org.example.lfmnacional.service.sesion;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.example.lfmnacional.service.CampeonatoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Deshace lo que dejo la importacion de una sesion, para poder volver a
 * importarla corregida ({@code POST /api/sesiones/reimportar}).
 *
 * <p>Lo que NO toca, a proposito:
 * <ul>
 *   <li>Incidentes autogenerados: pueden estar en revision por los comisarios, y
 *       la reimportacion no los duplica porque se deduplican por
 *       {@code clave_origen}.</li>
 *   <li>Logros ya otorgados: no hay forma de saber si se ganaron por esta
 *       carrera o por otra.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DeshacerSesionService {

    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final VueltaRepository vueltaRepository;
    private final SancionRepository sancionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final CampeonatoService campeonatoService;

    /**
     * Lo que hay que recordar de la carrera anterior para dejar la nueva igual:
     * si sus resultados sumaban al campeonato. Un campeonato cerrado no suma
     * resultados nuevos, pero corregir una carrera que ya sumaba no puede hacer
     * que deje de sumar.
     */
    public record EstadoAnterior(Boolean sumaAlCampeonato) {
    }

    /**
     * Saca los resultados de la carrera y revierte el Elo y el SR que dieron,
     * dejando el movimiento inverso en el historial (igual que al revertir una
     * sancion).
     *
     * <p>Se bloquea si la carrera tiene sanciones: las de puestos y segundos
     * estan aplicadas sobre estos resultados, y no hay como trasladarlas a los
     * nuevos sin saber si siguen correspondiendo.
     *
     * <p>Limite conocido: el Elo de un piloto en carreras posteriores se calculo
     * con el que tenia despues de esta. Revertir resta lo que gano aca, pero no
     * recalcula esas otras carreras.
     */
    @Transactional
    public EstadoAnterior deshacerCarrera(Carrera carrera) {
        if (sancionRepository.existsByCarrera_Id(carrera.getId())) {
            throw new BusinessException("La carrera " + carrera.getNombre()
                    + " tiene sanciones cargadas. Borralas antes de reimportar la sesion de carrera.");
        }
        List<ResultadoCarrera> resultados =
                resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId());

        Boolean suma = resultados.stream()
                .map(ResultadoCarrera::getSumaAlCampeonato)
                .filter(Objects::nonNull)
                .reduce(Boolean::logicalAnd)
                .orElse(null);

        List<Usuario> usuarios = new ArrayList<>();
        List<EloSancion> elo = new ArrayList<>();
        List<SafetyRatingSancion> sr = new ArrayList<>();
        String motivo = "Reimportacion de la carrera " + carrera.getNombre() + ": se revierte el resultado anterior";
        for (ResultadoCarrera resultado : resultados) {
            Usuario usuario = resultado.getUsuario();
            int cambioElo = resultado.getEloGanado() != null ? resultado.getEloGanado() : 0;
            int cambioSr = resultado.getSrGanado() != null ? resultado.getSrGanado() : 0;
            usuario.setElo(Math.max(0, usuario.getElo() - cambioElo));
            usuario.setSafetyRating(Math.max(0, usuario.getSafetyRating() - cambioSr));
            usuarios.add(usuario);
            elo.add(EloSancion.builder().usuario(usuario).cambio(-cambioElo).motivo(motivo).carrera(carrera).build());
            sr.add(SafetyRatingSancion.builder().usuario(usuario).cambio(-cambioSr).motivo(motivo).carrera(carrera).build());
        }
        usuarioRepository.saveAll(usuarios);
        eloSancionRepository.saveAll(elo);
        safetyRatingSancionRepository.saveAll(sr);

        resultadoCarreraRepository.deleteAll(resultados);
        vueltaRepository.deleteByCarrera_IdAndTipo(carrera.getId(), "RACE");
        // Hibernate inserta antes de borrar dentro de un mismo flush: sin esto,
        // los resultados nuevos (mismo carrera + usuario) chocan con el unique.
        resultadoCarreraRepository.flush();
        return new EstadoAnterior(suma);
    }

    /**
     * Deja los resultados nuevos sumando (o no) como los anteriores y rehace la
     * tabla: {@code actualizarPuntos} acumula, y sin recalcular quedarian
     * sumados los puntos viejos y los nuevos.
     */
    @Transactional
    public void restaurarCampeonato(Carrera carrera, EstadoAnterior anterior) {
        if (carrera.getCampeonato() == null) {
            return;
        }
        if (anterior.sumaAlCampeonato() != null) {
            List<ResultadoCarrera> nuevos =
                    resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId());
            nuevos.forEach(r -> r.setSumaAlCampeonato(anterior.sumaAlCampeonato()));
            resultadoCarreraRepository.saveAll(nuevos);
        }
        campeonatoService.recalcularPuntos(carrera.getCampeonato().getId());
    }

    /**
     * La clasificacion se importa pisando piloto por piloto: un piloto que no
     * esta en el JSON corregido quedaria con su tiempo viejo.
     */
    @Transactional
    public void deshacerClasificacion(Carrera carrera) {
        sesionClasificacionRepository.deleteByCarrera_Id(carrera.getId());
        vueltaRepository.deleteByCarrera_IdAndTipo(carrera.getId(), "QUALIFY");
        sesionClasificacionRepository.flush();
    }

    /**
     * La pole de los resultados se toma de la clasificacion al importar la
     * carrera. Si se reimporta la clasificacion con la carrera ya cargada, hay
     * que volver a marcarla.
     */
    @Transactional
    public void recalcularPole(Carrera carrera) {
        Long poleUsuarioId = sesionClasificacionRepository.findByCarrera_IdOrderByTiempoAsc(carrera.getId())
                .stream().findFirst()
                .map(SesionClasificacion::getUsuario)
                .map(Usuario::getId)
                .orElse(null);
        List<ResultadoCarrera> resultados =
                resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carrera.getId());
        resultados.forEach(r -> r.setPoles(r.getUsuario().getId().equals(poleUsuarioId)));
        resultadoCarreraRepository.saveAll(resultados);
    }
}
