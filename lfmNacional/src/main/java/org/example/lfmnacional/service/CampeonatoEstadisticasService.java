package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.EstadisticasCampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.PilotoCampeonatoEstadisticasResponse;
import org.example.lfmnacional.dto.campeonato.RondaCampeonatoEstadisticasResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoMiembro;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Incidente;
import org.example.lfmnacional.entity.IncidentePiloto;
import org.example.lfmnacional.entity.Inscripcion;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Sancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.enums.EstadoIncidente;
import org.example.lfmnacional.enums.EstadoInscripcion;
import org.example.lfmnacional.enums.RolPilotoIncidente;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CarreraRepository;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Estadisticas agregadas de un campeonato, para su organizador.
 *
 * <p>Se agrega en memoria y no con una consulta por piloto: un campeonato son
 * unas 30 carreras por 24 pilotos, o sea del orden de 700 resultados, y agruparlos
 * en Java cuesta menos que una docena de countBy distintos. Es el mismo criterio
 * que usa {@link VueltaService#resumenCarrera}.
 *
 * <p>Los puntos y las posiciones NO se recalculan: se leen de
 * {@code CampeonatoPosicion}, que es la tabla que ya alimenta el endpoint de la
 * tabla de posiciones. Si se recalcularan aca, las dos vistas podrian divergir.
 *
 * <p>Sin cache, igual que {@link EstadisticasService}: cambian en cada import de
 * sesion y no vale la pena un cache que hay que acordarse de evictar.
 */
@Service
@RequiredArgsConstructor
public class CampeonatoEstadisticasService {

    private final CarreraRepository carreraRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final InscripcionRepository inscripcionRepository;
    private final IncidenteRepository incidenteRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final SancionRepository sancionRepository;
    private final CampeonatoMiembroRepository miembroRepository;
    private final CampeonatoPosicionRepository posicionRepository;
    private final UsuarioRepository usuarioRepository;

    /** Estados en los que una carrera todavia no se corrio. */
    private static final Set<EstadoCarrera> PENDIENTES = Set.of(
            EstadoCarrera.PROGRAMADA,
            EstadoCarrera.INSCRIPCIONES_ABIERTAS,
            EstadoCarrera.INSCRIPCIONES_CERRADAS);

    @Transactional(readOnly = true)
    public EstadisticasCampeonatoResponse estadisticas(Campeonato campeonato) {
        Long id = campeonato.getId();

        List<Carrera> carreras = carreraRepository.findByCampeonato_IdOrderByFechaDesc(id);
        List<ResultadoCarrera> resultados = resultadoCarreraRepository.findByCarrera_Campeonato_Id(id);
        List<Inscripcion> inscripciones = inscripcionRepository.findByCarrera_Campeonato_Id(id);
        List<Incidente> incidentes = incidenteRepository.findByCarrera_Campeonato_Id(id);
        List<IncidentePiloto> incidentesPiloto =
                incidentePilotoRepository.findByIncidente_Carrera_Campeonato_Id(id);
        List<Sancion> sanciones = sancionesDelCampeonato(id);
        List<CampeonatoMiembro> miembros = miembroRepository.findByCampeonato_IdOrderByFechaAltaAsc(id);
        List<CampeonatoPosicion> posiciones = posicionRepository.findByCampeonato_IdOrderByPuntosDesc(id);

        Map<Long, List<ResultadoCarrera>> porCarrera = agruparPorCarrera(resultados);
        Map<Long, List<Inscripcion>> porCarreraInscripcion = agruparPorCarrera(inscripciones);
        Map<Long, Long> incidentesPorCarrera = contarPor(
                incidentes, i -> i.getCarrera().getId());
        Map<Long, Long> sancionesPorCarrera = contarPor(sanciones, this::carreraDeSancion);
        Map<Long, Long> incidentesCausante = contarPor(incidentesPiloto.stream()
                        .filter(ip -> ip.getRol() == RolPilotoIncidente.CAUSANTE)
                        .toList(),
                ip -> ip.getUsuario().getId());
        Map<Long, Long> sancionesPorPiloto = contarPor(sanciones, s -> s.getUsuario().getId());

        Map<Long, Acumulado> porPiloto = new HashMap<>();
        for (Map.Entry<Long, List<ResultadoCarrera>> e : porCarrera.entrySet()) {
            Set<Long> conVueltaRapida = pilotosConVueltaRapida(e.getValue());
            for (ResultadoCarrera r : e.getValue()) {
                porPiloto.computeIfAbsent(r.getUsuario().getId(), k -> new Acumulado())
                        .sumar(r, conVueltaRapida.contains(r.getUsuario().getId()));
            }
        }

        long participaciones = resultados.size();
        long abandono = resultados.stream().filter(r -> !r.isFinalizo()).count();

        List<PilotoCampeonatoEstadisticasResponse> pilotos = construirPilotos(
                miembros, posiciones, porPiloto, incidentesCausante, sancionesPorPiloto, id);
        List<RondaCampeonatoEstadisticasResponse> rondas = new ArrayList<>();
        for (Carrera carrera : carreras) {
            rondas.add(construirRonda(
                    carrera,
                    porCarrera.getOrDefault(carrera.getId(), List.of()),
                    porCarreraInscripcion.getOrDefault(carrera.getId(), List.of()),
                    incidentesPorCarrera.getOrDefault(carrera.getId(), 0L),
                    sancionesPorCarrera.getOrDefault(carrera.getId(), 0L)));
        }

        return new EstadisticasCampeonatoResponse(
                id,
                campeonato.getNombre(),
                campeonato.getTemporada(),
                campeonato.getCategoria() == null ? null : campeonato.getCategoria().getNombre(),
                campeonato.getEstado() == null ? null : campeonato.getEstado().name(),
                carreras.size(),
                contarCarreras(carreras, Set.of(EstadoCarrera.FINALIZADA)),
                contarCarreras(carreras, Set.of(EstadoCarrera.EN_CURSO)),
                contarCarreras(carreras, PENDIENTES),
                contarCarreras(carreras, Set.of(EstadoCarrera.CANCELADA)),
                miembros.size(),
                porPiloto.size(),
                participaciones,
                abandono,
                porcentaje(participaciones - abandono, participaciones),
                incidentes.size(),
                incidentes.stream().filter(i -> i.getEstado() == EstadoIncidente.PENDIENTE).count(),
                sanciones.size(),
                rondas,
                pilotos);
    }

    // ------------------------------------------------------------- por piloto

    /**
     * Una fila por piloto. La union es de tres fuentes porque ninguna sola
     * alcanza: un piloto puede estar habilitado y no haber corrido nunca (esta
     * solo en miembros), o haber corrido y todavia no estar en la tabla de puntos
     * (esta solo en resultados).
     */
    private List<PilotoCampeonatoEstadisticasResponse> construirPilotos(
            List<CampeonatoMiembro> miembros,
            List<CampeonatoPosicion> posiciones,
            Map<Long, Acumulado> porPiloto,
            Map<Long, Long> incidentesCausante,
            Map<Long, Long> sanciones,
            Long campeonatoId) {

        Map<Long, Integer> posicionesPorPiloto = new HashMap<>();
        Map<Long, Integer> puntosPorPiloto = new HashMap<>();
        for (CampeonatoPosicion p : posiciones) {
            Long usuarioId = p.getUsuario().getId();
            posicionesPorPiloto.put(usuarioId, p.getPosicion());
            puntosPorPiloto.put(usuarioId, p.getPuntos());
        }

        Map<Long, UsuarioRef> refs = new HashMap<>();
        for (CampeonatoMiembro m : miembros) {
            refs.put(m.getUsuario().getId(), new UsuarioRef(m.getUsuario()));
        }
        // Los que aparecen en resultados o en la tabla de puntos sin estar
        // inscriptos (carga manual de resultados, piloto dado de baja despues)
        // igual tienen que salir. Para ellos se busca el usuario: si se inventan
        // nombres y ratings la tabla miente.
        Set<Long> sinFicha = new HashSet<>(porPiloto.keySet());
        sinFicha.addAll(posicionesPorPiloto.keySet());
        sinFicha.removeAll(refs.keySet());
        if (!sinFicha.isEmpty()) {
            for (Usuario u : usuarioRepository.findAllById(sinFicha)) {
                refs.put(u.getId(), new UsuarioRef(u));
            }
            sinFicha.removeAll(refs.keySet());
        }
        // Solo si el usuario ya no existe en la base.
        for (Long usuarioId : sinFicha) {
            refs.put(usuarioId, new UsuarioRef(null, null, 0, 0));
        }

        List<PilotoCampeonatoEstadisticasResponse> filas = new ArrayList<>();
        for (Map.Entry<Long, UsuarioRef> e : refs.entrySet()) {
            Long usuarioId = e.getKey();
            UsuarioRef ref = e.getValue();
            Acumulado acc = porPiloto.getOrDefault(usuarioId, new Acumulado());

            filas.add(new PilotoCampeonatoEstadisticasResponse(
                    usuarioId,
                    ref.nombrePiloto != null ? ref.nombrePiloto : "Piloto #" + usuarioId,
                    ref.fotoPerfil,
                    ref.elo,
                    ref.safetyRating,
                    posicionesPorPiloto.getOrDefault(usuarioId, 0),
                    puntosPorPiloto.getOrDefault(usuarioId, 0),
                    acc.carrerasDisputadas,
                    acc.victorias,
                    acc.podios,
                    acc.poles,
                    acc.vueltasRapidas,
                    acc.abandono,
                    porcentaje(acc.carrerasDisputadas - acc.abandono, acc.carrerasDisputadas),
                    acc.eloGanado,
                    acc.srGanado,
                    incidentesCausante.getOrDefault(usuarioId, 0L),
                    sanciones.getOrDefault(usuarioId, 0L)));
        }

        // Mismo criterio que la tabla de posiciones: primero por posicion
        // (1, 2, 3... y los sin puntuar al final, por eso MAX_VALUE), y a igual
        // de posicion el que mas rindio en pista.
        filas.sort(Comparator
                .comparingInt((PilotoCampeonatoEstadisticasResponse p) ->
                        p.posicion() == 0 ? Integer.MAX_VALUE : p.posicion())
                .thenComparing(PilotoCampeonatoEstadisticasResponse::puntos, Comparator.reverseOrder())
                .thenComparing(PilotoCampeonatoEstadisticasResponse::victorias, Comparator.reverseOrder())
                .thenComparing(PilotoCampeonatoEstadisticasResponse::carrerasDisputadas, Comparator.reverseOrder())
                .thenComparing(PilotoCampeonatoEstadisticasResponse::nombrePiloto));
        return filas;
    }

    // -------------------------------------------------------------- por ronda

    private RondaCampeonatoEstadisticasResponse construirRonda(
            Carrera carrera,
            List<ResultadoCarrera> resultados,
            List<Inscripcion> inscripciones,
            long incidentes,
            long sanciones) {

        long inscriptos = inscripciones.stream()
                .filter(i -> i.getEstado() == EstadoInscripcion.INSCRIPTO)
                .count();
        long presentes = resultados.size();
        // Nunca negativo: un piloto puede tener resultado sin inscripcion cargada
        // (carga manual de resultados), y en ese caso no es un "ausente".
        long ausentes = Math.max(0, inscriptos - presentes);
        long abandono = resultados.stream().filter(r -> !r.isFinalizo()).count();

        ResultadoCarrera ganador = resultados.stream()
                .filter(r -> Objects.equals(r.getPosicionFinal(), 1))
                .findFirst()
                .orElse(null);
        ResultadoCarrera mejorVuelta = resultados.stream()
                .filter(r -> r.getVueltaRapida() != null && r.getVueltaRapida() > 0)
                .min(Comparator.comparingLong(ResultadoCarrera::getVueltaRapida))
                .orElse(null);

        return new RondaCampeonatoEstadisticasResponse(
                carrera.getId(),
                carrera.getNombre(),
                carrera.getCircuito(),
                carrera.getFecha(),
                carrera.getEstado() == null ? null : carrera.getEstado().name(),
                carrera.getCupoMaximo() != null ? carrera.getCupoMaximo() : 0,
                inscriptos,
                presentes,
                ausentes,
                abandono,
                incidentes,
                sanciones,
                ganador != null ? ganador.getUsuario().getNombrePiloto() : null,
                mejorVuelta != null ? mejorVuelta.getVueltaRapida() : null,
                mejorVuelta != null ? mejorVuelta.getUsuario().getNombrePiloto() : null);
    }

    // ------------------------------------------------------------------ utils

    /**
     * La vuelta rapida de una carrera es el minimo de {@code vueltaRapida} entre
     * los que participaron en ella, no un flag que se pueda confiar fila por
     * fila. Con dos pilotos empatados entran los dos, igual que en
     * {@code ResultadoCarreraRepository.countVueltaRapidaByUsuario}.
     */
    private Set<Long> pilotosConVueltaRapida(List<ResultadoCarrera> resultados) {
        Long minimo = resultados.stream()
                .map(ResultadoCarrera::getVueltaRapida)
                .filter(v -> v != null && v > 0)
                .min(Long::compareTo)
                .orElse(null);
        if (minimo == null) {
            return Set.of();
        }
        return resultados.stream()
                .filter(r -> Objects.equals(r.getVueltaRapida(), minimo))
                .map(r -> r.getUsuario().getId())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Sanciones del campeonato: las que cuelgan de una carrera y las que
     * nacen de la resolucion de un incidente, que no tienen carrera puesta y son
     * las que genera la importacion de sesiones. Se deduplican por id porque
     * una sancion puede tener las dos cosas.
     */
    private List<Sancion> sancionesDelCampeonato(Long campeonatoId) {
        Map<Long, Sancion> porId = new LinkedHashMap<>();
        for (Sancion s : sancionRepository.findByCarrera_Campeonato_Id(campeonatoId)) {
            porId.put(s.getId(), s);
        }
        for (Sancion s : sancionRepository.findByResolucion_Incidente_Carrera_Campeonato_Id(campeonatoId)) {
            porId.putIfAbsent(s.getId(), s);
        }
        return List.copyOf(porId.values());
    }

    /** Carrera a la que se le carga la sancion, sea directa o via incidente. */
    private Long carreraDeSancion(Sancion s) {
        if (s.getCarrera() != null) {
            return s.getCarrera().getId();
        }
        if (s.getResolucion() != null && s.getResolucion().getIncidente() != null
                && s.getResolucion().getIncidente().getCarrera() != null) {
            return s.getResolucion().getIncidente().getCarrera().getId();
        }
        return null;
    }

    private long contarCarreras(List<Carrera> carreras, Set<EstadoCarrera> estados) {
        return carreras.stream().filter(c -> estados.contains(c.getEstado())).count();
    }

    private double porcentaje(long parte, long total) {
        return total == 0 ? 0.0 : (double) parte / total * 100;
    }

    /** Agrupa por la id de la carrera de cada elemento. */
    private <T> Map<Long, List<T>> agruparPorCarrera(List<T> items) {
        Map<Long, List<T>> mapa = new HashMap<>();
        for (T item : items) {
            mapa.computeIfAbsent(carreraIdDe(item), k -> new ArrayList<>()).add(item);
        }
        return mapa;
    }

    private Long carreraIdDe(Object item) {
        if (item instanceof ResultadoCarrera r) {
            return r.getCarrera().getId();
        }
        if (item instanceof Inscripcion i) {
            return i.getCarrera().getId();
        }
        throw new IllegalArgumentException("tipo sin carrera: " + item.getClass());
    }

    /**
     * Cuenta por la clave que devuelve {@code clave}. Los elementos con clave
     * {@code null} se descartan: pasa con una sancion que no se puede atribuir a
     * ninguna carrera de este campeonato.
     */
    private <T> Map<Long, Long> contarPor(List<T> items, Function<T, Long> clave) {
        Map<Long, Long> mapa = new HashMap<>();
        for (T item : items) {
            Long k = clave.apply(item);
            if (k != null) {
                mapa.merge(k, 1L, Long::sum);
            }
        }
        return mapa;
    }

    /** Fila de la tabla por piloto en construccion. */
    private static final class Acumulado {
        private long carrerasDisputadas;
        private long victorias;
        private long podios;
        private long poles;
        private long vueltasRapidas;
        private long abandono;
        private int eloGanado;
        private int srGanado;

        private void sumar(ResultadoCarrera r, boolean vueltaRapida) {
            carrerasDisputadas++;
            if (r.getPosicionFinal() != null) {
                if (r.getPosicionFinal() == 1) {
                    victorias++;
                }
                if (r.getPosicionFinal() <= 3) {
                    podios++;
                }
            }
            if (r.isPoles()) {
                poles++;
            }
            if (vueltaRapida) {
                vueltasRapidas++;
            }
            if (!r.isFinalizo()) {
                abandono++;
            }
            eloGanado += r.getEloGanado() != null ? r.getEloGanado() : 0;
            srGanado += r.getSrGanado() != null ? r.getSrGanado() : 0;
        }
    }

    /** Datos del piloto copiados de la entidad para no arrastrar la relacion perezosa. */
    private record UsuarioRef(String nombrePiloto, String fotoPerfil, int elo, int safetyRating) {
        private UsuarioRef(Usuario usuario) {
            this(usuario.getNombrePiloto(), usuario.getFotoPerfil(), usuario.getElo(),
                    usuario.getSafetyRating());
        }
    }
}