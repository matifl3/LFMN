package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.EstadoCarrera;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.service.rating.EloCalculator;
import org.example.lfmnacional.service.rating.SrCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ResultadoCarreraService {

    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraService carreraService;
    private final UsuarioService usuarioService;
    private final CampeonatoService campeonatoService;
    private final LogroService logroService;
    private final EloCalculator eloCalculator;
    private final SrCalculator srCalculator;

    public ResultadoCarrera getEntity(Long id) {
        return resultadoCarreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public ResultadoCarreraResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ResultadoCarreraResponse> listarPorCarrera(Long carreraId) {
        return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carreraId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ResultadoCarreraResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return resultadoCarreraRepository.findByUsuario_Id(usuarioId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public List<ResultadoCarreraResponse> cargarResultados(CargarResultadosRequest request) {
        Carrera carrera = carreraService.getEntity(request.carreraId());
        validarCarga(carrera);
        if (resultadoCarreraRepository.existsByCarrera_Id(carrera.getId())) {
            throw new BusinessException(
                    "Ya existen resultados cargados para la carrera " + carrera.getNombre());
        }

        List<ResultadoCarrera> resultados = new ArrayList<>();
        for (ResultadoCarreraRequest item : request.resultados()) {
            Usuario usuario = usuarioService.getEntity(item.usuarioId());
            ResultadoCarrera resultado = resultadoCarreraRepository
                    .findByCarrera_IdAndUsuario_Id(carrera.getId(), usuario.getId())
                    .orElseGet(() -> ResultadoCarrera.builder()
                            .carrera(carrera)
                            .usuario(usuario)
                            .build());
            resultado.setPosicionFinal(item.posicionFinal());
            resultado.setTiempoTotal(item.tiempoTotal());
            resultado.setVueltaRapida(item.vueltaRapida());
            resultado.setPoles(item.poles());
            resultado.setFinalizo(item.finalizo() != null && item.finalizo());
            resultado.setModeloAuto(item.modeloAuto());
            resultado.setSkinAuto(item.skinAuto());
            resultados.add(resultado);
        }
        resultadoCarreraRepository.saveAll(resultados);

        recalcularEloYSafetyRating(carrera, resultados);
        campeonatoService.actualizarPuntos(carrera, resultados);
        for (ResultadoCarrera resultado : resultados) {
            logroService.evaluarLogros(resultado.getUsuario());
        }
        return resultados.stream().map(this::toResponse).toList();
    }

    private void validarCarga(Carrera carrera) {
        if (carrera.getEstado() == EstadoCarrera.CANCELADA) {
            throw new BusinessException("No se pueden cargar resultados de una carrera cancelada");
        }
    }

    private void recalcularEloYSafetyRating(Carrera carrera, List<ResultadoCarrera> resultados) {
        Map<Long, Integer> elos = new HashMap<>();
        for (ResultadoCarrera resultado : resultados) {
            elos.put(resultado.getUsuario().getId(), resultado.getUsuario().getElo());
        }
        List<Usuario> usuariosActualizar = new ArrayList<>();
        List<EloSancion> eloSanciones = new ArrayList<>();
        List<SafetyRatingSancion> srSanciones = new ArrayList<>();
        for (ResultadoCarrera resultado : resultados) {
            Integer posicion = resultado.getPosicionFinal();
            if (posicion == null) {
                continue;
            }
            Integer eloPropio = elos.get(resultado.getUsuario().getId());
            List<Integer> rivales = elos.entrySet().stream()
                    .filter(e -> !e.getKey().equals(resultado.getUsuario().getId()))
                    .map(Map.Entry::getValue)
                    .toList();
            int cambioElo = eloCalculator.calcularCambio(eloPropio, posicion, resultados.size(), rivales);
            boolean finalizo = resultado.getFinalizo() != null && resultado.getFinalizo();
            int cambioSr = srCalculator.calcularCambio(finalizo, posicion);

            resultado.setEloGanado(cambioElo);
            resultado.setSrGanado(cambioSr);

            Usuario usuario = resultado.getUsuario();
            usuario.setElo(Math.max(0, usuario.getElo() + cambioElo));
            usuario.setSafetyRating(Math.max(0, usuario.getSafetyRating() + cambioSr));
            usuariosActualizar.add(usuario);

            eloSanciones.add(EloSancion.builder()
                    .usuario(usuario)
                    .cambio(cambioElo)
                    .motivo("Resultado carrera " + carrera.getNombre() + " (posicion " + posicion + ")")
                    .carrera(carrera)
                    .build());
            srSanciones.add(SafetyRatingSancion.builder()
                    .usuario(usuario)
                    .cambio(cambioSr)
                    .motivo("Resultado carrera " + carrera.getNombre() + " (posicion " + posicion + ")")
                    .carrera(carrera)
                    .build());
        }
        resultadoCarreraRepository.saveAll(resultados);
        usuarioRepository.saveAll(usuariosActualizar);
        eloSancionRepository.saveAll(eloSanciones);
        safetyRatingSancionRepository.saveAll(srSanciones);
    }

    private ResultadoCarreraResponse toResponse(ResultadoCarrera resultado) {
        return new ResultadoCarreraResponse(
                resultado.getId(),
                resultado.getCarrera().getId(),
                resultado.getCarrera().getNombre(),
                resultado.getCarrera().getCampeonato().getCategoria().getNombre(),
                resultado.getUsuario().getId(),
                resultado.getUsuario().getNombrePiloto(),
                resultado.getPosicionFinal(),
                resultado.getTiempoTotal(),
                resultado.getVueltaRapida(),
                resultado.getModeloAuto(),
                resultado.getSkinAuto(),
                resultado.getPoles(),
                resultado.getFinalizo(),
                resultado.getEloGanado(),
                resultado.getSrGanado());
    }
}
