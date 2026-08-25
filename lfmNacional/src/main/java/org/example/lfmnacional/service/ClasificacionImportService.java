package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.CarSesionData;
import org.example.lfmnacional.dto.sesion.ResultadoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.SesionClasificacion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClasificacionImportService {

    private static final long SIN_VUELTA_VALIDA = 999999999L;

    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraResolverService carreraResolverService;

    @Transactional
    public void importarClasificacion(Carrera carrera, SesionServerData sesion) {
        if (sesion.result() == null) {
            return;
        }
        List<ResultadoSesionData> conTiempo = sesion.result().stream()
                .filter(r -> carreraResolverService.guidValido(r.driverGuid()))
                .filter(r -> r.bestLap() != null && r.bestLap() > 0 && r.bestLap() < SIN_VUELTA_VALIDA)
                .sorted(Comparator.comparing(ResultadoSesionData::bestLap))
                .toList();
        if (conTiempo.isEmpty()) {
            return;
        }

        Long tiempoPole = conTiempo.get(0).bestLap();
        Map<Integer, CarSesionData> autos = autosPorId(sesion);
        for (ResultadoSesionData dato : conTiempo) {
            Usuario usuario = usuarioRepository.findByGuidSteam(dato.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            SesionClasificacion clasificacion = sesionClasificacionRepository
                    .findByCarrera_IdAndUsuario_Id(carrera.getId(), usuario.getId())
                    .orElseGet(() -> SesionClasificacion.builder()
                            .carrera(carrera)
                            .usuario(usuario)
                            .build());
            clasificacion.setFecha(LocalDateTime.now());
            clasificacion.setTiempo(dato.bestLap());
            clasificacion.setDiferenciaPole(dato.bestLap() - tiempoPole);
            clasificacion.setModeloAuto(modeloDe(autos, dato));
            clasificacion.setSkinAuto(skinDe(autos, dato));
            sesionClasificacionRepository.save(clasificacion);
        }
    }

    Map<Integer, CarSesionData> autosPorId(SesionServerData sesion) {
        Map<Integer, CarSesionData> mapa = new HashMap<>();
        if (sesion.cars() != null) {
            for (CarSesionData auto : sesion.cars()) {
                if (auto.carId() != null) {
                    mapa.put(auto.carId(), auto);
                }
            }
        }
        return mapa;
    }

    String modeloDe(Map<Integer, CarSesionData> autos, ResultadoSesionData dato) {
        if (dato.carModel() != null && !dato.carModel().isBlank()) {
            return dato.carModel();
        }
        CarSesionData auto = dato.carId() != null ? autos.get(dato.carId()) : null;
        return auto != null ? auto.model() : null;
    }

    String skinDe(Map<Integer, CarSesionData> autos, ResultadoSesionData dato) {
        CarSesionData auto = dato.carId() != null ? autos.get(dato.carId()) : null;
        return auto != null ? auto.skin() : null;
    }
}
