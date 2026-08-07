package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sancion.RealPenaltyEventRequest;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.entity.*;
import org.example.lfmnacional.enums.OrigenSancion;
import org.example.lfmnacional.enums.TipoNotificacion;
import org.example.lfmnacional.enums.TipoSancion;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SancionService {

    private static final int SR_PENALIDAD_RP = -10;

    private final SancionRepository sancionRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CarreraService carreraService;

    public Sancion getEntity(Long id) {
        return sancionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sancion no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    public SancionResponse getById(Long id) {
        return toResponse(getEntity(id));
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
    public SancionResponse create(SancionRequest request) {
        Sancion sancion = buildSancion(request);
        sancion = sancionRepository.save(sancion);
        aplicarEfectos(sancion);
        notificar(sancion);
        return toResponse(sancion);
    }

    @Transactional
    public SancionResponse update(Long id, SancionRequest request) {
        Sancion sancion = getEntity(id);
        sancion.setUsuario(usuarioService.getEntity(request.usuarioId()));
        sancion.setCarrera(request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null);
        sancion.setTipo(request.tipo());
        sancion.setValor(request.valor());
        sancion.setMotivo(request.motivo());
        sancion.setOrigen(request.origen());
        sancion.setIdExterno(request.idExterno());
        sancion.setFecha(request.fecha() != null ? request.fecha() : sancion.getFecha());
        return toResponse(sancionRepository.save(sancion));
    }

    @Transactional
    public void delete(Long id) {
        sancionRepository.delete(getEntity(id));
    }

    @Transactional
    public SancionResponse recibirEventoRealPenalty(RealPenaltyEventRequest request) {
        if (sancionRepository.existsByOrigenAndIdExterno(OrigenSancion.REAL_PENALTY, request.eventoId())) {
            Sancion existente = sancionRepository
                    .findByOrigenAndIdExterno(OrigenSancion.REAL_PENALTY, request.eventoId())
                    .orElseThrow();
            return toResponse(existente);
        }
        Usuario usuario = usuarioRepository.findByGuidSteam(request.driverGUID())
                .orElseThrow(() -> new BusinessException(
                        "No se pudo correlacionar el driverGUID " + request.driverGUID() + " con ningun usuario"));

        Sancion sancion = Sancion.builder()
                .usuario(usuario)
                .tipo(mapTipoRp(request.tipo()))
                .valor(request.segundos())
                .motivo(request.motivo())
                .origen(OrigenSancion.REAL_PENALTY)
                .idExterno(request.eventoId())
                .fecha(request.timestamp() != null ? request.timestamp() : LocalDateTime.now())
                .build();
        sancion = sancionRepository.save(sancion);

        int cambioSr = (sancion.getTipo() == TipoSancion.DESCALIFICACION) ? SR_PENALIDAD_RP * 2 : SR_PENALIDAD_RP;
        aplicarCambioSafetyRating(usuario, cambioSr, "Penalidad automatica Real Penalty", sancion.getCarrera());
        notificar(sancion);
        return toResponse(sancion);
    }

    private Sancion buildSancion(SancionRequest request) {
        Usuario usuario = usuarioService.getEntity(request.usuarioId());
        Carrera carrera = request.carreraId() != null ? carreraService.getEntity(request.carreraId()) : null;
        return Sancion.builder()
                .usuario(usuario)
                .carrera(carrera)
                .tipo(request.tipo())
                .valor(request.valor())
                .motivo(request.motivo())
                .origen(request.origen())
                .idExterno(request.idExterno())
                .fecha(request.fecha() != null ? request.fecha() : LocalDateTime.now())
                .build();
    }

    private void aplicarEfectos(Sancion sancion) {
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
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
        resultado.setPosicionFinal(resultado.getPosicionFinal() + sancion.getValor());
        resultadoCarreraRepository.save(resultado);
        reordenarResultados(sancion.getCarrera().getId());
    }

    private void aplicarSegundos(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
        resultado.setTiempoTotal(resultado.getTiempoTotal() + sancion.getValor() * 1000L);
        resultadoCarreraRepository.save(resultado);
        reordenarResultados(sancion.getCarrera().getId());
    }

    private void reordenarResultados(Long carreraId) {
        List<ResultadoCarrera> resultados = resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carreraId);
        List<ResultadoCarrera> ordenados = resultados.stream()
                .sorted(Comparator.comparing(
                        ResultadoCarrera::getPosicionFinal,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
        int pos = 1;
        for (ResultadoCarrera resultado : ordenados) {
            if (resultado.getPosicionFinal() == null) {
                continue;
            }
            resultado.setPosicionFinal(pos++);
            resultadoCarreraRepository.save(resultado);
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

    private TipoSancion mapTipoRp(String tipo) {
        if (tipo == null) {
            return TipoSancion.SEGUNDOS;
        }
        return switch (tipo.toLowerCase()) {
            case "dt" -> TipoSancion.DRIVE_THROUGH;
            case "sg" -> TipoSancion.STOP_AND_GO;
            case "dsq" -> TipoSancion.DESCALIFICACION;
            default -> TipoSancion.SEGUNDOS;
        };
    }

    private SancionResponse toResponse(Sancion sancion) {
        return new SancionResponse(
                sancion.getId(),
                sancion.getUsuario().getId(),
                sancion.getCarrera() != null ? sancion.getCarrera().getId() : null,
                sancion.getResolucion() != null ? sancion.getResolucion().getId() : null,
                sancion.getTipo(),
                sancion.getValor(),
                sancion.getMotivo(),
                sancion.getOrigen(),
                sancion.getIdExterno(),
                sancion.getFecha());
    }
}
