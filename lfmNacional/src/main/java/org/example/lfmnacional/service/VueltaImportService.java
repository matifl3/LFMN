package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.sesion.LapSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.entity.VueltaCarrera;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VueltaImportService {

    private final VueltaRepository vueltaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraResolverService carreraResolverService;

    @Transactional
    public void importarVueltas(Carrera carrera, SesionServerData sesion, String tipo) {
        if (sesion.laps() == null || sesion.laps().isEmpty()) {
            return;
        }
        vueltaRepository.deleteByCarrera_IdAndTipo(carrera.getId(), tipo);
        Map<String, Integer> numeros = new HashMap<>();
        List<VueltaCarrera> vueltas = new ArrayList<>();
        for (LapSesionData lap : sesion.laps()) {
            if (!carreraResolverService.guidValido(lap.driverGuid())) {
                continue;
            }
            Usuario usuario = usuarioRepository.findByGuidSteam(lap.driverGuid()).orElse(null);
            if (usuario == null) {
                continue;
            }
            int numero = numeros.merge(lap.driverGuid(), 1, Integer::sum);
            vueltas.add(VueltaCarrera.builder()
                    .carrera(carrera)
                    .usuario(usuario)
                    .numeroVuelta(numero)
                    .tiempoMs(lap.lapTime())
                    .sector1(sectorDe(lap, 0))
                    .sector2(sectorDe(lap, 1))
                    .sector3(sectorDe(lap, 2))
                    .cortes(lap.cuts())
                    .neumatico(lap.tyre())
                    .tipo(tipo)
                    .build());
        }
        vueltaRepository.saveAll(vueltas);
    }

    private Long sectorDe(LapSesionData lap, int indice) {
        if (lap.sectors() == null || lap.sectors().size() <= indice) {
            return null;
        }
        Integer sector = lap.sectors().get(indice);
        return sector != null ? sector.longValue() : null;
    }
}
