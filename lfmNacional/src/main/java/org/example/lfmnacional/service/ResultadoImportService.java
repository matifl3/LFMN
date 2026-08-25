package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.resultado.CargarResultadosRequest;
import org.example.lfmnacional.dto.resultado.ResultadoCarreraRequest;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResultadoImportService {

    private static final long SIN_VUELTA_VALIDA = 999999999L;
    private static final long SIN_TIEMPO = 0L;

    private final ResultadoCarreraService resultadoCarreraService;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraResolverService carreraResolverService;
    private final ClasificacionImportService clasificacionImportService;

    @Transactional
    public void importarResultados(Carrera carrera, SesionServerData sesion) {
        if (sesion.result() == null) {
            return;
        }
        List<ResultadoSesionData> conGuid = sesion.result().stream()
                .filter(r -> carreraResolverService.guidValido(r.driverGuid()))
                .toList();
        if (conGuid.isEmpty()) {
            return;
        }

        List<ResultadoSesionData> finalizaron = conGuid.stream()
                .filter(r -> r.totalTime() != null && r.totalTime() > SIN_TIEMPO)
                .sorted(java.util.Comparator.comparing(ResultadoSesionData::totalTime))
                .toList();
        List<ResultadoSesionData> dnf = conGuid.stream()
                .filter(r -> r.totalTime() == null || r.totalTime() <= SIN_TIEMPO)
                .sorted(java.util.Comparator.comparing(
                        (ResultadoSesionData r) -> vueltasCompletadas(sesion, r.driverGuid()),
                        java.util.Comparator.reverseOrder()))
                .toList();

        List<ResultadoCarreraRequest> items = new ArrayList<>();
        Map<Integer, org.example.lfmnacional.dto.sesion.CarSesionData> autos = clasificacionImportService.autosPorId(sesion);
        int posicion = 1;
        for (ResultadoSesionData dato : finalizaron) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            items.add(new ResultadoCarreraRequest(
                    carrera.getId(), usuario.getId(), posicion++,
                    dato.totalTime(), vueltaRapidaValida(dato.bestLap()),
                    null, true, null, null,
                    clasificacionImportService.modeloDe(autos, dato),
                    clasificacionImportService.skinDe(autos, dato)));
        }
        for (ResultadoSesionData dato : dnf) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            items.add(new ResultadoCarreraRequest(
                    carrera.getId(), usuario.getId(), posicion++,
                    dato.totalTime(), vueltaRapidaValida(dato.bestLap()),
                    null, false, null, null,
                    clasificacionImportService.modeloDe(autos, dato),
                    clasificacionImportService.skinDe(autos, dato)));
        }

        if (items.isEmpty()) {
            return;
        }

        Optional<SesionClasificacion> poleOpt = sesionClasificacionRepository
                .findByCarrera_IdOrderByTiempoAsc(carrera.getId()).stream().findFirst();
        if (poleOpt.isPresent()) {
            Long poleUsuarioId = poleOpt.get().getUsuario().getId();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).usuarioId().equals(poleUsuarioId)) {
                    ResultadoCarreraRequest old = items.get(i);
                    items.set(i, new ResultadoCarreraRequest(
                            old.carreraId(), old.usuarioId(), old.posicionFinal(),
                            old.tiempoTotal(), old.vueltaRapida(),
                            true, old.finalizo(), old.eloGanado(), old.srGanado(),
                            old.modeloAuto(), old.skinAuto()));
                    break;
                }
            }
        }

        resultadoCarreraService.cargarResultados(new CargarResultadosRequest(carrera.getId(), items));
    }

    private Long vueltaRapidaValida(Long bestLap) {
        if (bestLap == null || bestLap <= 0 || bestLap >= SIN_VUELTA_VALIDA) {
            return null;
        }
        return bestLap;
    }

    private long vueltasCompletadas(SesionServerData sesion, String guid) {
        if (sesion.laps() == null) {
            return 0;
        }
        return sesion.laps().stream()
                .filter(l -> guid.equals(l.driverGuid()))
                .count();
    }
}
