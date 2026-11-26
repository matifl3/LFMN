package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.entity.*;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.EntityMapper;
import org.example.lfmnacional.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class SancionService {

    private final SancionRepository sancionRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final NotificacionRepository notificacionRepository;
    private final ApelacionRepository apelacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final UsuarioService usuarioService;
    private final CarreraService carreraService;
    private final CampeonatoService campeonatoService;

    public Sancion getEntity(Long id) {
        return sancionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sancion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SancionResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<SancionResponse> listAll(Pageable pageable) {
        return sancionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SancionResponse> listarPorUsuario(Long usuarioId) {
        return sancionRepository.findByUsuario_IdOrderByFechaDesc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SancionResponse> listarPorCarrera(Long carreraId) {
        return sancionRepository.findByCarrera_Id(carreraId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public SancionResponse create(SancionRequest request) {
        Sancion sancion = buildSancion(request);
        sancion = sancionRepository.save(sancion);
        aplicarEfectos(sancion);
        notificar(sancion);
        return toResponse(sancion);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public SancionResponse update(Long id, SancionRequest request) {
        Sancion sancion = getEntity(id);
        Usuario nuevoUsuario = usuarioService.getEntity(request.usuarioId());
        Carrera nuevaCarrera = request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null;

        boolean afectaEfectos = sancion.getTipo() != request.tipo()
                || !Objects.equals(sancion.getValor(), request.valor())
                || !Objects.equals(sancion.getUsuario().getId(), nuevoUsuario.getId())
                || !Objects.equals(
                        sancion.getCarrera() != null ? sancion.getCarrera().getId() : null,
                        request.carreraId());
        if (afectaEfectos) {
            revertirEfectos(sancion);
        }

        sancion.setUsuario(nuevoUsuario);
        sancion.setCarrera(nuevaCarrera);
        sancion.setTipo(request.tipo());
        sancion.setValor(request.valor());
        sancion.setMotivo(request.motivo());
        sancion.setOrigen(request.origen());
        sancion.setIdExterno(request.idExterno());
        sancion.setFecha(request.fecha() != null ? request.fecha() : sancion.getFecha());
        sancion = sancionRepository.save(sancion);

        if (afectaEfectos) {
            aplicarEfectos(sancion);
        }
        return toResponse(sancion);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id) {
        Sancion sancion = getEntity(id);
        if (apelacionRepository.existsBySancion_Id(id)) {
            throw new BusinessException("No se puede eliminar la sancion porque tiene apelaciones asociadas");
        }
        if (sancion.getResolucion() != null) {
            throw new BusinessException(
                    "No se puede eliminar la sancion porque esta asociada a una resolucion de incidente");
        }
        revertirEfectos(sancion);
        sancionRepository.delete(sancion);
    }

    private Sancion buildSancion(SancionRequest request) {
        Usuario usuario = usuarioService.getEntity(request.usuarioId());
        Carrera carrera = request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null;
        ResolucionIncidente resolucion = request.resolucionId() != null
                ? resolucionIncidenteRepository.findById(request.resolucionId())
                        .orElse(null)
                : null;
        return Sancion.builder()
                .usuario(usuario)
                .carrera(carrera)
                .resolucion(resolucion)
                .tipo(request.tipo())
                .valor(request.valor())
                .motivo(request.motivo())
                .origen(request.origen())
                .idExterno(request.idExterno())
                .fecha(request.fecha() != null ? request.fecha() : LocalDateTime.now())
                .build();
    }

    private void aplicarEfectos(Sancion sancion) {
        sancion.setEfectosAplicados(true);
        switch (sancion.getTipo()) {
            case ELO -> aplicarCambioElo(sancion.getUsuario(), sancion.getValor(),
                    sancion.getMotivo(), sancion.getCarrera());
            case SAFETY_RATING -> aplicarCambioSafetyRating(sancion.getUsuario(), sancion.getValor(),
                    sancion.getMotivo(), sancion.getCarrera());
            case PUESTOS -> aplicarPerdidaPuestos(sancion);
            case SEGUNDOS -> aplicarSegundos(sancion);
            default -> {
            }
        }
    }

    /**
     * Revertir es escribir elo/safetyRating al reves, asi que tambien tiene que
     * evictar "usuarios".
     *
     * Ojo con el self-invocation: update() y delete() la llaman con
     * this.revertirEfectos(...) desde dentro de su propio cuerpo, y eso no pasa
     * por el proxy, asi que el evict de esta anotacion NO se dispara en esos dos
     * caminos. Por eso update() y delete() llevan su propio evict.
     * Esta anotacion cubre el unico llamador externo: ApelacionService, cuando
     * aprueba una apelacion.
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    public void revertirEfectos(Sancion sancion) {
        if (!Boolean.TRUE.equals(sancion.getEfectosAplicados())) {
            return;
        }
        switch (sancion.getTipo()) {
            case ELO -> revertirCambioElo(sancion.getUsuario(), sancion.getValor(), sancion);
            case SAFETY_RATING -> revertirCambioSafetyRating(sancion.getUsuario(), sancion.getValor(), sancion);
            case PUESTOS -> revertirPerdidaPuestos(sancion);
            case SEGUNDOS -> revertirSegundos(sancion);
            default -> {
            }
        }
        sancion.setEfectosAplicados(false);
        sancionRepository.save(sancion);
    }

    private void aplicarCambioElo(Usuario usuario, Integer cambio, String motivo, Carrera carrera) {
        int valor = cambio != null ? cambio : 0;
        usuario.setElo(usuario.getElo() + valor);
        usuarioRepository.save(usuario);
        eloSancionRepository.save(EloSancion.builder()
                .usuario(usuario)
                .cambio(valor)
                .motivo(motivo != null ? motivo : "Ajuste manual de Elo")
                .carrera(carrera)
                .build());
    }

    private void aplicarCambioSafetyRating(Usuario usuario, Integer cambio, String motivo, Carrera carrera) {
        int valor = cambio != null ? cambio : 0;
        usuario.setSafetyRating(usuario.getSafetyRating() + valor);
        usuarioRepository.save(usuario);
        safetyRatingSancionRepository.save(SafetyRatingSancion.builder()
                .usuario(usuario)
                .cambio(valor)
                .motivo(motivo != null ? motivo : "Ajuste manual de Safety Rating")
                .carrera(carrera)
                .build());
    }

    private void aplicarPerdidaPuestos(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        resultadoDe(sancion);
        reclasificar(sancion.getCarrera());
    }

    private void aplicarSegundos(Sancion sancion) {
        ajustarTiempo(sancion, true);
    }

    /**
     * Una penalizacion de tiempo tiene que <em>reclasificar</em>: el piloto cae
     * en la clasificacion segun su tiempo total nuevo, no se queda en el puesto
     * que tenia con un tiempo peor.
     *
     * <p>Sin tiempo total no hay contra que comparar, asi que en vez de un NPE
     * (sumarle a null) se corta con un mensaje claro.
     */
    private void ajustarTiempo(Sancion sancion, boolean sumar) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = resultadoDe(sancion);
        if (resultado.getTiempoTotal() == null) {
            throw new BusinessException("El usuario no tiene tiempo total en la carrera indicada");
        }
        long delta = Math.abs((long) sancion.getValor()) * 1000L;
        resultado.setTiempoTotal(sumar ? resultado.getTiempoTotal() + delta : resultado.getTiempoTotal() - delta);
        resultadoCarreraRepository.save(resultado);
        if (!sumar) {
            // reclasificar() aplica las sanciones activas: la que se revierte ya no cuenta.
            sancion.setEfectosAplicados(false);
        }
        reclasificar(sancion.getCarrera());
    }

    private void revertirPerdidaPuestos(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        // reclasificar() aplica las sanciones activas: la que se revierte ya no cuenta.
        sancion.setEfectosAplicados(false);
        reclasificar(sancion.getCarrera());
    }

    private void revertirSegundos(Sancion sancion) {
        ajustarTiempo(sancion, false);
    }

    /**
     * Orden real de llegada: los que terminaron adelante de los que no, y entre
     * los que terminaron primero el que hizo mas vueltas y despues el de menor
     * tiempo. El tiempo solo no alcanza porque el de un doblado es el de sus
     * vueltas, que son menos, y puede ser menor que el del ganador. Entre los
     * que no terminaron no se compara nada: el sort es estable y quedan en el
     * orden original.
     */
    private static final Comparator<ResultadoCarrera> ORDEN_DE_LLEGADA = (a, b) -> {
        if (a.isFinalizo() != b.isFinalizo()) {
            return a.isFinalizo() ? -1 : 1;
        }
        if (!a.isFinalizo()) {
            return 0;
        }
        return Comparator
                .comparing(ResultadoCarrera::getVueltasCompletadas, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ResultadoCarrera::getTiempoTotal, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(a, b);
    };

    /**
     * Rehace la clasificacion de la carrera desde cero cada vez que se aplica o
     * revierte una sancion de PUESTOS o SEGUNDOS.
     *
     * <p>Antes cada sancion movia la clasificacion que habia dejado la anterior:
     * una de segundos reordenaba toda la carrera solo por tiempo (un doblado o un
     * DNF con tiempo 0 podian quedar 1ros) y deshacia en silencio una perdida de
     * puestos ya aplicada; al revertir esa perdida el piloto subia desde un lugar
     * que ya no era el penalizado. Partiendo siempre de la posicion original, el
     * resultado depende solo de que sanciones estan activas, no del orden en que
     * se aplicaron o revirtieron.
     *
     * <ol>
     *   <li>Orden base: {@code posicionOriginal}, la de la carga.</li>
     *   <li>Si hay alguna sancion de segundos activa, el tiempo cambio y se
     *       reordena por {@link #ORDEN_DE_LLEGADA}. Si no hay, no se toca: una
     *       carga manual puede tener un orden que no sale de los tiempos.</li>
     *   <li>Las perdidas de puestos activas, de la mas vieja a la mas nueva.</li>
     * </ol>
     *
     * <p>Los resultados sin posicion quedan afuera, como estaban.
     */
    private void reclasificar(Carrera carrera) {
        List<ResultadoCarrera> clasificacion = resultadosDeCarrera(carrera.getId()).stream()
                .filter(r -> r.getPosicionOriginal() != null || r.getPosicionFinal() != null)
                .collect(Collectors.toCollection(ArrayList::new));
        for (ResultadoCarrera resultado : clasificacion) {
            // Filas cargadas antes de existir la columna: la posicion actual es
            // la original mientras no tengan perdidas de puestos ya aplicadas.
            if (resultado.getPosicionOriginal() == null) {
                resultado.setPosicionOriginal(resultado.getPosicionFinal());
            }
        }
        clasificacion.sort(Comparator.comparing(ResultadoCarrera::getPosicionOriginal));

        List<Sancion> activas = sancionRepository.findByCarrera_Id(carrera.getId()).stream()
                .filter(s -> Boolean.TRUE.equals(s.getEfectosAplicados()) && s.getValor() != null)
                .sorted(Comparator.comparing(Sancion::getFecha, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Sancion::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (activas.stream().anyMatch(s -> s.getTipo() == TipoSancion.SEGUNDOS)) {
            clasificacion.sort(ORDEN_DE_LLEGADA);
        }
        for (Sancion sancion : activas) {
            if (sancion.getTipo() == TipoSancion.PUESTOS) {
                bajarPuestos(clasificacion, sancion.getUsuario().getId(), Math.abs(sancion.getValor()));
            }
        }

        renumerar(clasificacion);
        resultadoCarreraRepository.saveAll(clasificacion);
        recalcularTablaDelCampeonato(carrera);
    }

    /**
     * Perder puestos NO es sumar el valor a {@code posicionFinal}: si el
     * sancionado cae sobre la posicion de otro quedan empatados y el renumerado
     * le da el mejor puesto al sancionado. Se lo mueve N lugares en la lista y
     * despues se renumera. Si no le quedan N lugares, cae ultimo. El valor se
     * toma absoluto: escribir "-2" no deberia ser un premio de dos puestos.
     */
    private void bajarPuestos(List<ResultadoCarrera> clasificacion, Long usuarioId, int puestos) {
        for (int origen = 0; origen < clasificacion.size(); origen++) {
            if (clasificacion.get(origen).getUsuario().getId().equals(usuarioId)) {
                int destino = Math.min(origen + puestos, clasificacion.size() - 1);
                clasificacion.add(destino, clasificacion.remove(origen));
                return;
            }
        }
    }

    private ResultadoCarrera resultadoDe(Sancion sancion) {
        return resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
    }

    /**
     * Mover posiciones o tiempos de una carrera cambia los puntos que el
     * campeonato le da a ese piloto, asi que la tabla se rehace. Sin esto la
     * sancion se ve reflejada en el resultado de la carrera y no en el
     * campeonato, que es donde se juega el campeonato.
     *
     * <p>Va como llamada a otro bean a proposito: el {@code @CacheEvict} de la
     * tabla de posiciones vive en {@code CampeonatoService}, y Spring solo lo
     * aplica si la llamada entra por su proxy, no desde adentro de la misma
     * clase.
     */
    private void recalcularTablaDelCampeonato(Carrera carrera) {
        if (carrera.getCampeonato() == null) {
            return;
        }
        campeonatoService.recalcularPuntos(carrera.getCampeonato().getId());
    }

    private void revertirCambioElo(Usuario usuario, Integer cambio, Sancion sancion) {
        int valor = cambio != null ? cambio : 0;
        usuario.setElo(usuario.getElo() - valor);
        usuarioRepository.save(usuario);
        eloSancionRepository.save(EloSancion.builder()
                .usuario(usuario)
                .cambio(-valor)
                .motivo("Reversion de sancion " + sancion.getId() + ": "
                        + (sancion.getMotivo() != null ? sancion.getMotivo() : "sancion"))
                .carrera(sancion.getCarrera())
                .build());
    }

    private void revertirCambioSafetyRating(Usuario usuario, Integer cambio, Sancion sancion) {
        int valor = cambio != null ? cambio : 0;
        usuario.setSafetyRating(usuario.getSafetyRating() - valor);
        usuarioRepository.save(usuario);
        safetyRatingSancionRepository.save(SafetyRatingSancion.builder()
                .usuario(usuario)
                .cambio(-valor)
                .motivo("Reversion de sancion " + sancion.getId() + ": "
                        + (sancion.getMotivo() != null ? sancion.getMotivo() : "sancion"))
                .carrera(sancion.getCarrera())
                .build());
    }

    /**
     * Los resultados de la carrera. El metodo del repository viene ordenado por
     * posicion, pero abajo cada helper lo reordena por lo que necesita, asi que
     * el nombre no dice mas que "traeme los resultados".
     */
    private List<ResultadoCarrera> resultadosDeCarrera(Long carreraId) {
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carreraId);
    }

    private void renumerar(List<ResultadoCarrera> clasificacion) {
        int pos = 1;
        for (ResultadoCarrera resultado : clasificacion) {
            resultado.setPosicionFinal(pos++);
        }
    }

    private void notificar(Sancion sancion) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(sancion.getUsuario())
                .tipo(TipoNotificacion.PENALIZACION)
                .mensaje("Recibiste una sancion: " + sancion.getTipo()
                        + (sancion.getMotivo() != null ? " - " + sancion.getMotivo() : ""))
                .leida(false)
                .build());
    }

    private SancionResponse toResponse(Sancion sancion) {
        var c = EntityMapper.resolveCarreraInfo(sancion.getCarrera());
        return new SancionResponse(
                sancion.getId(),
                sancion.getUsuario().getId(),
                c != null ? c.id() : null,
                c != null ? c.nombre() : null,
                c != null ? c.categoriaNombre() : null,
                sancion.getResolucion() != null ? sancion.getResolucion().getId() : null,
                sancion.getTipo(),
                sancion.getValor(),
                sancion.getMotivo(),
                sancion.getOrigen(),
                sancion.getIdExterno(),
                sancion.getFecha());
    }
}
