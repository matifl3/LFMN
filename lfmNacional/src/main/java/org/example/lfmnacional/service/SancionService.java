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
        moverEnClasificacion(sancion, Math.abs(sancion.getValor() != null ? sancion.getValor() : 0));
    }

    /**
     * Perder puestos NO es sumar el valor a {@code posicionFinal}. Si el
     * sancionado cae justo sobre la posicion de otro, los dos quedan empatados y
     * el renumerado por numero le termina dando el puesto mejor al sancionado:
     * en una carrera de 4, un 2do con 2 puestos de penalizacion terminaba 3ro en
     * vez de 4to, o sea la penalizacion se perdia a medias.
     *
     * <p>Lo que corresponde es moverlo N lugares en la lista de la clasificacion
     * y renumerar desde 1, que es como se aplica una perdida de puestos. El
     * valor se toma absoluto: en un stewart penalty siempre se pierden lugares,
     * escribir "-2" no deberia ser un premio de dos puestos.
     */
    private void moverEnClasificacion(Sancion sancion, int desplazamiento) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        List<ResultadoCarrera> clasificacion = resultadosClasificados(sancion.getCarrera().getId());
        int origen = indiceDe(clasificacion, sancion);
        int destino = Math.min(Math.max(origen + desplazamiento, 0), clasificacion.size() - 1);
        ResultadoCarrera resultado = clasificacion.remove(origen);
        clasificacion.add(destino, resultado);
        renumerar(clasificacion);
        resultadoCarreraRepository.saveAll(clasificacion);
        recalcularTablaDelCampeonato(sancion.getCarrera());
    }

    private void aplicarSegundos(Sancion sancion) {
        ajustarTiempo(sancion, true);
    }

    /**
     * Una penalizacion de tiempo tiene que <em>reclasificar</em>: el piloto cae
     * en la clasificacion segun su tiempo total nuevo, no en el puesto que tenia
     * antes. Antes solo se le sumaba el tiempo y se renumeraba por
     * {@code posicionFinal}, que no cambia, asi que la penalizacion no movia a
     * nadie de lugar: el piloto quedaba con un tiempo peor y el mismo puesto.
     *
     * <p>Sin tiempo total no hay contra que comparar, asi que en vez de un NPE
     * (sumarle a null) se corta con un mensaje claro.
     */
    private void ajustarTiempo(Sancion sancion, boolean sumar) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
        if (resultado.getTiempoTotal() == null) {
            throw new BusinessException("El usuario no tiene tiempo total en la carrera indicada");
        }
        long delta = Math.abs((long) sancion.getValor()) * 1000L;
        resultado.setTiempoTotal(sumar ? resultado.getTiempoTotal() + delta : resultado.getTiempoTotal() - delta);
        resultadoCarreraRepository.save(resultado);
        reclasificarPorTiempo(sancion.getCarrera().getId());
        recalcularTablaDelCampeonato(sancion.getCarrera());
    }

    /** Reordena por tiempo total ascendente y renumera. Los que no tienen tiempo quedan afuera. */
    private void reclasificarPorTiempo(Long carreraId) {
        List<ResultadoCarrera> porTiempo = resultadosDeCarrera(carreraId).stream()
                .filter(r -> r.getTiempoTotal() != null)
                .sorted(Comparator.comparing(ResultadoCarrera::getTiempoTotal))
                .collect(Collectors.toCollection(ArrayList::new));
        renumerar(porTiempo);
        resultadoCarreraRepository.saveAll(porTiempo);
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

    private void revertirPerdidaPuestos(Sancion sancion) {
        moverEnClasificacion(sancion, -Math.abs(sancion.getValor() != null ? sancion.getValor() : 0));
    }

    private void revertirSegundos(Sancion sancion) {
        ajustarTiempo(sancion, false);
    }

    /**
     * Los resultados de la carrera. El metodo del repository viene ordenado por
     * posicion, pero abajo cada helper lo reordena por lo que necesita, asi que
     * el nombre no dice mas que "traeme los resultados".
     */
    private List<ResultadoCarrera> resultadosDeCarrera(Long carreraId) {
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carreraId);
    }

    /** Los que tienen posicion asignada, del 1ero al ultimo. Los demas se dejan como estan. */
    private List<ResultadoCarrera> resultadosClasificados(Long carreraId) {
        return resultadosDeCarrera(carreraId).stream()
                .filter(r -> r.getPosicionFinal() != null)
                .sorted(Comparator.comparing(ResultadoCarrera::getPosicionFinal))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private int indiceDe(List<ResultadoCarrera> clasificacion, Sancion sancion) {
        for (int i = 0; i < clasificacion.size(); i++) {
            if (clasificacion.get(i).getUsuario().getId().equals(sancion.getUsuario().getId())) {
                return i;
            }
        }
        throw new BusinessException("El usuario no tiene resultado en la carrera indicada");
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
