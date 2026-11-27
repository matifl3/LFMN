package org.example.lfmnacional.service;

import org.example.lfmnacional.repository.ResumenResultados;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.LogroRequest;
import org.example.lfmnacional.dto.logro.LogroResponse;
import org.example.lfmnacional.dto.logro.RecompensaRequest;
import org.example.lfmnacional.dto.logro.RecompensaResponse;
import org.example.lfmnacional.dto.logro.UsuarioLogroResponse;
import org.example.lfmnacional.entity.Logro;
import org.example.lfmnacional.entity.Notificacion;
import org.example.lfmnacional.entity.Recompensa;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.UsuarioLogro;
import org.example.lfmnacional.entity.UsuarioRecompensa;
import org.example.lfmnacional.enums.TipoCondicionLogro;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.LogroRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.RecompensaRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.UsuarioLogroRepository;
import org.example.lfmnacional.repository.UsuarioRecompensaRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogroService {

    private final LogroRepository logroRepository;
    private final RecompensaRepository recompensaRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;
    private final UsuarioRecompensaRepository usuarioRecompensaRepository;
    private final NotificacionRepository notificacionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public Logro getEntity(Long id) {
        return logroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logro no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "logros", key = "#id")
    public LogroResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("logros")
    public List<LogroResponse> listAll() {
        return logroRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "logros", allEntries = true)
    public LogroResponse create(LogroRequest request) {
        if (logroRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("Ya existe un logro con el nombre " + request.nombre());
        }
        Logro logro = Logro.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .tipoCondicion(request.tipoCondicion())
                .valorCondicion(request.valorCondicion())
                .icono(request.icono())
                .build();
        logro = logroRepository.save(logro);
        evaluarParaTodos();
        return toResponse(logro);
    }

    @Transactional
    @CacheEvict(value = "logros", allEntries = true)
    public LogroResponse update(Long id, LogroRequest request) {
        Logro logro = getEntity(id);
        if (!logro.getNombre().equals(request.nombre()) && logroRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("Ya existe un logro con el nombre " + request.nombre());
        }
        logro.setNombre(request.nombre());
        logro.setDescripcion(request.descripcion());
        logro.setTipoCondicion(request.tipoCondicion());
        logro.setValorCondicion(request.valorCondicion());
        logro.setIcono(request.icono());
        logro = logroRepository.save(logro);
        evaluarParaTodos();
        return toResponse(logro);
    }

    /**
     * Un logro nuevo (o con el umbral cambiado) se evalua contra el historial que
     * ya existe: si no, un piloto con 25 carreras veia "0 / 20" hasta correr la
     * siguiente. La liga es chica, asi que recorrer todos los usuarios es barato.
     */
    private void evaluarParaTodos() {
        for (Usuario usuario : usuarioRepository.findAll()) {
            evaluarLogros(usuario);
        }
    }

    @Transactional
    @CacheEvict(value = "logros", allEntries = true)
    public void delete(Long id) {
        Logro logro = getEntity(id);
        for (Recompensa recompensa : logro.getRecompensas()) {
            usuarioRecompensaRepository.deleteByRecompensa_Id(recompensa.getId());
        }
        usuarioLogroRepository.deleteByLogro_Id(id);
        logroRepository.delete(logro);
    }

    @Transactional
    public RecompensaResponse agregarRecompensa(Long logroId, RecompensaRequest request) {
        Logro logro = getEntity(logroId);
        Recompensa recompensa = Recompensa.builder()
                .logro(logro)
                .descripcion(request.descripcion())
                .tipo(request.tipo())
                .build();
        return toRecompensaResponse(recompensaRepository.save(recompensa));
    }

    @Transactional
    public void quitarRecompensa(Long logroId, Long recompensaId) {
        Logro logro = getEntity(logroId);
        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id " + recompensaId));
        if (!recompensa.getLogro().getId().equals(logro.getId())) {
            throw new BusinessException("La recompensa no pertenece al logro indicado");
        }
        usuarioRecompensaRepository.deleteByRecompensa_Id(recompensaId);
        recompensaRepository.delete(recompensa);
    }

    /**
     * El progreso se calcula en vivo y no se lee de {@code usuario_logro}: esa
     * tabla solo se actualiza en los caminos que llaman a evaluarLogros, y
     * cualquier otro cambio en los resultados (o en el Elo) dejaba la barra
     * desactualizada. La tabla queda como registro de "obtenido" y su fecha.
     */
    @Transactional(readOnly = true)
    public List<UsuarioLogroResponse> listarLogrosUsuario(Long usuarioId) {
        Usuario usuario = usuarioService.getEntity(usuarioId);
        List<Logro> logros = logroRepository.findAll();
        Map<Long, UsuarioLogro> existentes = usuarioLogroRepository
                .findByUsuario_IdOrderByLogro_Id(usuarioId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        ul -> ul.getLogro().getId(), ul -> ul));
        MetricasPiloto metricas = new MetricasPiloto(usuario);
        List<UsuarioLogroResponse> response = new ArrayList<>();
        for (Logro logro : logros) {
            long valor = metricas.valor(logro.getTipoCondicion());
            UsuarioLogro guardado = existentes.get(logro.getId());
            boolean obtenido = (guardado != null && Boolean.TRUE.equals(guardado.getObtenido()))
                    || valor >= logro.getValorCondicion();
            int progreso = obtenido
                    ? logro.getValorCondicion()
                    : (int) Math.min(valor, logro.getValorCondicion());
            response.add(new UsuarioLogroResponse(
                    logro.getId(),
                    logro.getNombre(),
                    logro.getDescripcion(),
                    logro.getTipoCondicion(),
                    logro.getValorCondicion(),
                    logro.getIcono(),
                    progreso,
                    obtenido,
                    guardado != null ? guardado.getFechaObtencion() : null));
        }
        return response;
    }

    @Transactional
    public void evaluarLogros(Usuario usuario) {
        List<Logro> logros = logroRepository.findAll();
        Map<Long, UsuarioLogro> existentes = usuarioLogroRepository
                .findByUsuario_IdOrderByLogro_Id(usuario.getId()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        ul -> ul.getLogro().getId(), ul -> ul));

        MetricasPiloto metricas = new MetricasPiloto(usuario);
        for (Logro logro : logros) {
            int valor = (int) metricas.valor(logro.getTipoCondicion());
            UsuarioLogro usuarioLogro = existentes.getOrDefault(logro.getId(),
                    UsuarioLogro.builder()
                            .logro(logro)
                            .usuario(usuario)
                            .progreso(0)
                            .obtenido(false)
                            .build());
            // Un logro ganado no se pierde: si el Elo baja de 1800 o una sancion
            // quita una victoria, el logro queda. Antes se recalculaba y volvia a
            // "ganarse" (con otra notificacion) cuando la metrica subia de nuevo.
            boolean yaObtenido = Boolean.TRUE.equals(usuarioLogro.getObtenido());
            boolean obtenido = yaObtenido || valor >= logro.getValorCondicion();
            boolean transicion = !yaObtenido && obtenido;
            usuarioLogro.setProgreso(obtenido ? logro.getValorCondicion() : Math.min(valor, logro.getValorCondicion()));
            usuarioLogro.setObtenido(obtenido);
            if (transicion) {
                usuarioLogro.setFechaObtencion(LocalDateTime.now());
            }
            usuarioLogroRepository.save(usuarioLogro);
            if (transicion) {
                notificarLogro(usuario, logro);
                otorgarRecompensas(usuario, logro);
            }
        }
    }

    /**
     * Metricas de un piloto durante UNA evaluacion de logros. Antes cada logro
     * disparaba su propio COUNT (N logros = N queries); ahora los conteos salen de
     * una sola query y las vueltas rapidas (query aparte, mas cara) solo se piden
     * si algun logro las usa.
     */
    private final class MetricasPiloto {
        private final Usuario usuario;
        private ResumenResultados resumen;
        private Long vueltasRapidas;

        MetricasPiloto(Usuario usuario) {
            this.usuario = usuario;
        }

        long valor(TipoCondicionLogro condicion) {
            return switch (condicion) {
                case VICTORIAS -> resumen().victorias();
                case PODIOS -> resumen().podios();
                case CARRERAS -> resumen().carreras();
                case POLES -> resumen().poles();
                case CARRERAS_COMPLETADAS -> resumen().finalizadas();
                case VUELTAS_RAPIDAS -> vueltasRapidas();
                case ELO -> usuario.getElo();
            };
        }

        private ResumenResultados resumen() {
            if (resumen == null) {
                resumen = resultadoCarreraRepository.resumenPorUsuario(usuario.getId());
            }
            return resumen;
        }

        private long vueltasRapidas() {
            if (vueltasRapidas == null) {
                vueltasRapidas = resultadoCarreraRepository.countVueltaRapidaByUsuario(usuario.getId());
            }
            return vueltasRapidas;
        }
    }

    private void otorgarRecompensas(Usuario usuario, Logro logro) {
        for (Recompensa recompensa : logro.getRecompensas()) {
            if (usuarioRecompensaRepository
                    .findByRecompensa_IdAndUsuario_Id(recompensa.getId(), usuario.getId()).isPresent()) {
                continue;
            }
            usuarioRecompensaRepository.save(UsuarioRecompensa.builder()
                    .recompensa(recompensa)
                    .usuario(usuario)
                    .reclamada(false)
                    .build());
            notificarRecompensa(usuario, recompensa);
        }
    }

    private void notificarLogro(Usuario usuario, Logro logro) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(usuario)
                .tipo(TipoNotificacion.LOGRO)
                .mensaje("Logro obtenido: " + logro.getNombre())
                .leida(false)
                .build());
    }

    private void notificarRecompensa(Usuario usuario, Recompensa recompensa) {
        notificacionRepository.save(Notificacion.builder()
                .usuario(usuario)
                .tipo(TipoNotificacion.RECOMPENSA)
                .mensaje("Recompensa disponible: " + recompensa.getDescripcion())
                .leida(false)
                .build());
    }

    private LogroResponse toResponse(Logro logro) {
        return new LogroResponse(
                logro.getId(),
                logro.getNombre(),
                logro.getDescripcion(),
                logro.getTipoCondicion(),
                logro.getValorCondicion(),
                logro.getIcono(),
                logro.getRecompensas().stream().map(this::toRecompensaResponse).toList());
    }

    private RecompensaResponse toRecompensaResponse(Recompensa recompensa) {
        return new RecompensaResponse(
                recompensa.getId(),
                recompensa.getLogro().getId(),
                recompensa.getDescripcion(),
                recompensa.getTipo());
    }
}
