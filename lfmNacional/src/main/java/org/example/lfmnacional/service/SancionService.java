package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sancion.SancionRequest;
import org.example.lfmnacional.dto.sancion.SancionResponse;
import org.example.lfmnacional.entity.*;
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
import java.util.Objects;

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
    public List<SancionResponse> listAll() {
        return sancionRepository.findAll().stream()
                .map(this::toResponse).toList();
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
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
        resultado.setPosicionFinal(resultado.getPosicionFinal() - sancion.getValor());
        resultadoCarreraRepository.save(resultado);
        reordenarResultados(sancion.getCarrera().getId());
    }

    private void revertirSegundos(Sancion sancion) {
        if (sancion.getCarrera() == null || sancion.getValor() == null) {
            return;
        }
        ResultadoCarrera resultado = resultadoCarreraRepository
                .findByCarrera_IdAndUsuario_Id(sancion.getCarrera().getId(), sancion.getUsuario().getId())
                .orElseThrow(() -> new BusinessException(
                        "El usuario no tiene resultado en la carrera indicada"));
        resultado.setTiempoTotal(resultado.getTiempoTotal() - sancion.getValor() * 1000L);
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
        }
        resultadoCarreraRepository.saveAll(ordenados);
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
        return new SancionResponse(
                sancion.getId(),
                sancion.getUsuario().getId(),
                sancion.getCarrera() != null ? sancion.getCarrera().getId() : null,
                sancion.getCarrera() != null ? sancion.getCarrera().getNombre() : null,
                sancion.getCarrera() != null ? sancion.getCarrera().getCampeonato().getCategoria().getNombre() : null,
                sancion.getResolucion() != null ? sancion.getResolucion().getId() : null,
                sancion.getTipo(),
                sancion.getValor(),
                sancion.getMotivo(),
                sancion.getOrigen(),
                sancion.getIdExterno(),
                sancion.getFecha());
    }
}
