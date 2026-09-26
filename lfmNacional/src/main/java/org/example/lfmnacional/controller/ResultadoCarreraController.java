package org.example.lfmnacional.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.PageResponse;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraResponse;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.CampeonatoAccesoService;
import org.example.lfmnacional.service.CarreraService;
import org.example.lfmnacional.service.ResultadoCarreraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadoCarreraController {

    private final ResultadoCarreraService resultadoCarreraService;
    private final CarreraService carreraService;
    private final CampeonatoAccesoService accesoService;

    @PostMapping("/cargar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMISARIO') or hasRole('ADMIN_CAMPEONATO')")
    public ResponseEntity<List<ResultadoCarreraResponse>> cargarResultados(
            @Valid @RequestBody CargarResultadosRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        exigirAdminDeLaCarrera(request.carreraId(), usuario);
        List<ResultadoCarreraResponse> respuestas = resultadoCarreraService.cargarResultados(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestas);
    }

    @GetMapping("/carrera/{carreraId}")
    public List<ResultadoCarreraResponse> listarPorCarrera(@PathVariable Long carreraId,
                                                           @AuthenticationPrincipal Usuario usuario) {
        accesoService.exigirVeCarrera(usuario, carreraService.getEntity(carreraId));
        return resultadoCarreraService.listarPorCarrera(carreraId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public PageResponse<ResultadoCarreraResponse> listarPorUsuario(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("carrera.fecha").descending());
        Page<ResultadoCarreraResponse> result = resultadoCarreraService.listarPorUsuario(usuarioId, pageable, usuario);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/{id}")
    public ResultadoCarreraResponse getById(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        ResultadoCarrera resultado = resultadoCarreraService.getEntity(id);
        Carrera carrera = resultado.getCarrera();
        accesoService.exigirVeCarrera(usuario, carrera);
        return resultadoCarreraService.getById(id);
    }

    /** El ADMIN global, un comisario o el dueno del campeonato de esa carrera. */
    private void exigirAdminDeLaCarrera(Long carreraId, Usuario usuario) {
        if (accesoService.esComisario(usuario)) {
            return;
        }
        accesoService.exigirAdministraCarrera(usuario, carreraService.getEntity(carreraId));
    }
}
