package org.example.lfmnacional.service;

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

    public Logro getEntity(Long id) {
        return logroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logro no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public LogroResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<LogroResponse> listAll() {
        return logroRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
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
        return toResponse(logroRepository.save(logro));
    }

    @Transactional
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
        return toResponse(logroRepository.save(logro));
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public List<UsuarioLogroResponse> listarLogrosUsuario(Long usuarioId) {
        usuarioService.getEntity(usuarioId);
        List<Logro> logros = logroRepository.findAll();
        Map<Long, UsuarioLogro> existentes = usuarioLogroRepository
                .findByUsuario_IdOrderByLogro_Id(usuarioId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        ul -> ul.getLogro().getId(), ul -> ul));
        List<UsuarioLogroResponse> response = new ArrayList<>();
        for (Logro logro : logros) {
            UsuarioLogro usuarioLogro = existentes.getOrDefault(logro.getId(),
                    UsuarioLogro.builder().progreso(0).obtenido(false).build());
            response.add(toUsuarioLogroResponse(logro, usuarioLogro));
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

        for (Logro logro : logros) {
            int valor = (int) calcularValorMetrica(logro.getTipoCondicion(), usuario);
            UsuarioLogro usuarioLogro = existentes.getOrDefault(logro.getId(),
                    UsuarioLogro.builder()
                            .logro(logro)
                            .usuario(usuario)
                            .progreso(0)
                            .obtenido(false)
                            .build());
            boolean obtenido = valor >= logro.getValorCondicion();
            boolean transicion = !Boolean.TRUE.equals(usuarioLogro.getObtenido()) && obtenido;
            usuarioLogro.setProgreso(Math.min(valor, logro.getValorCondicion()));
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

    private long calcularValorMetrica(TipoCondicionLogro condicion, Usuario usuario) {
        Long usuarioId = usuario.getId();
        return switch (condicion) {
            case VICTORIAS -> resultadoCarreraRepository.countByUsuario_IdAndPosicionFinal(usuarioId, 1);
            case PODIOS -> resultadoCarreraRepository.countByUsuario_IdAndPosicionFinalLessThanEqual(usuarioId, 3);
            case CARRERAS -> resultadoCarreraRepository.countByUsuario_Id(usuarioId);
            case POLES -> resultadoCarreraRepository.countByUsuario_IdAndPolesTrue(usuarioId);
            case VUELTAS_RAPIDAS -> resultadoCarreraRepository.countVueltaRapidaByUsuario(usuarioId);
            case CARRERAS_COMPLETADAS -> resultadoCarreraRepository.countByUsuario_IdAndFinalizoTrue(usuarioId);
            case ELO -> usuario.getElo();
        };
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

    private UsuarioLogroResponse toUsuarioLogroResponse(Logro logro, UsuarioLogro usuarioLogro) {
        return new UsuarioLogroResponse(
                logro.getId(),
                logro.getNombre(),
                logro.getDescripcion(),
                logro.getTipoCondicion(),
                logro.getValorCondicion(),
                usuarioLogro.getProgreso(),
                usuarioLogro.getObtenido(),
                usuarioLogro.getFechaObtencion());
    }
}
