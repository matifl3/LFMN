package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * La tabla de posiciones cacheada, sin ningun control de acceso.
 *
 * <p>Esta en su propio bean para que el control de acceso pueda vivir en el
 * servicio: con {@code @Cacheable} la cache se consulta antes de ejecutar el
 * cuerpo, asi que el control no puede ir adentro del metodo cacheado (un no
 * miembro que encuentra la cache caliente se lo saltearia). Y si el metodo
 * cacheado estuviera en la misma clase que el control, la llamada no pasaria
 * por el proxy y el cache no se aplicaria. Por eso:
 * {@code CampeonatoService.getTabla(id, visor)} controla adentro de su
 * transaccion y llama a este bean.
 *
 * <p>No exponer esto a controllers: el que llama es responsable del acceso.
 * Los {@code @CacheEvict} de {@code tabla_posiciones} siguen en
 * {@code CampeonatoService}: el evict es por nombre de cache, no por bean.
 */
@Service
@RequiredArgsConstructor
public class TablaPosicionesService {

    private final CampeonatoPosicionRepository campeonatoPosicionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "tabla_posiciones", key = "#campeonatoId")
    public List<TablaPosicionResponse> tabla(Long campeonatoId) {
        return campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(campeonatoId).stream()
                .map(TablaPosicionesService::toResponse)
                .toList();
    }

    private static TablaPosicionResponse toResponse(CampeonatoPosicion posicion) {
        return new TablaPosicionResponse(
                posicion.getUsuario().getId(),
                posicion.getUsuario().getNombrePiloto(),
                posicion.getPuntos(),
                posicion.getPosicion());
    }
}
