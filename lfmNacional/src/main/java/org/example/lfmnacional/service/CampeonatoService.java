package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.campeonato.CampeonatoRequest;
import org.example.lfmnacional.dto.campeonato.CampeonatoResponse;
import org.example.lfmnacional.dto.campeonato.TablaPosicionResponse;
import org.example.lfmnacional.entity.Campeonato;
import org.example.lfmnacional.entity.CampeonatoPosicion;
import org.example.lfmnacional.entity.Carrera;
import org.example.lfmnacional.entity.ResultadoCarrera;
import org.example.lfmnacional.enums.EstadoCampeonato;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampeonatoService {

    private static final List<Integer> PUNTOS_POR_POSICION =
            List.of(25, 18, 15, 12, 10, 8, 6, 4, 2, 1);

    private final CampeonatoRepository campeonatoRepository;
    private final CampeonatoPosicionRepository campeonatoPosicionRepository;
    private final CategoriaService categoriaService;

    public Campeonato getEntity(Long id) {
        return campeonatoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "campeonatos", key = "#id")
    public CampeonatoResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("campeonatos")
    public List<CampeonatoResponse> listAll() {
        return campeonatoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CampeonatoResponse> porCategoria(Long categoriaId) {
        return campeonatoRepository.findByCategoria_Id(categoriaId).stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "campeonatos", allEntries = true)
    public CampeonatoResponse create(CampeonatoRequest request) {
        Campeonato campeonato = Campeonato.builder()
                .nombre(request.nombre())
                .temporada(request.temporada())
                .categoria(categoriaService.getEntity(request.categoriaId()))
                .estado(request.estado())
                .sistemaPuntos(request.sistemaPuntos())
                .build();
        return toResponse(campeonatoRepository.save(campeonato));
    }

    @Transactional
    @CacheEvict(value = "campeonatos", allEntries = true)
    public CampeonatoResponse update(Long id, CampeonatoRequest request) {
        Campeonato campeonato = getEntity(id);
        campeonato.setNombre(request.nombre());
        campeonato.setTemporada(request.temporada());
        campeonato.setCategoria(categoriaService.getEntity(request.categoriaId()));
        campeonato.setEstado(request.estado() != null ? request.estado() : campeonato.getEstado());
        campeonato.setSistemaPuntos(request.sistemaPuntos());
        return toResponse(campeonatoRepository.save(campeonato));
    }

    @Transactional
    @CacheEvict(value = "campeonatos", allEntries = true)
    public CampeonatoResponse cerrar(Long id) {
        Campeonato campeonato = getEntity(id);
        campeonato.setEstado(EstadoCampeonato.CERRADO);
        return toResponse(campeonatoRepository.save(campeonato));
    }

    @Transactional
    @CacheEvict(value = "campeonatos", allEntries = true)
    public void delete(Long id) {
        Campeonato campeonato = getEntity(id);
        if (campeonatoPosicionRepository.existsByCampeonato_Id(id)) {
            throw new BusinessException(
                    "No se puede eliminar el campeonato '" + campeonato.getNombre()
                            + "' porque ya tiene posiciones calculadas en la tabla de puntos");
        }
        campeonatoRepository.delete(campeonato);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tabla_posiciones", key = "#id")
    public List<TablaPosicionResponse> getTabla(Long id) {
        getEntity(id);
        return campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(id).stream()
                .map(this::toTablaPosicion)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "tabla_posiciones", key = "#carrera.campeonato.id")
    public void actualizarPuntos(Carrera carrera, List<ResultadoCarrera> resultados) {
        Campeonato campeonato = carrera.getCampeonato();
        if (campeonato.getEstado() != EstadoCampeonato.ACTIVO) {
            return;
        }
        List<CampeonatoPosicion> aGuardar = new java.util.ArrayList<>();
        for (ResultadoCarrera resultado : resultados) {
            if (resultado.getPosicionFinal() == null) {
                continue;
            }
            int puntos = puntosPorPosicion(resultado.getPosicionFinal());
            CampeonatoPosicion posicion = campeonatoPosicionRepository
                    .findByCampeonato_IdAndUsuario_Id(campeonato.getId(), resultado.getUsuario().getId())
                    .orElseGet(() -> {
                        long total = campeonatoPosicionRepository.countByCampeonato_Id(campeonato.getId());
                        return CampeonatoPosicion.builder()
                                .campeonato(campeonato)
                                .usuario(resultado.getUsuario())
                                .puntos(0)
                                .posicion((int) total + 1)
                                .build();
                    });
            posicion.setPuntos(posicion.getPuntos() + puntos);
            aGuardar.add(posicion);
        }
        campeonatoPosicionRepository.saveAll(aGuardar);
        recalcularPosiciones(campeonato.getId());
    }

    private void recalcularPosiciones(Long campeonatoId) {
        List<CampeonatoPosicion> ordenadas =
                campeonatoPosicionRepository.findByCampeonato_IdOrderByPuntosDesc(campeonatoId);
        int rank = 1;
        for (CampeonatoPosicion posicion : ordenadas) {
            posicion.setPosicion(rank++);
        }
        campeonatoPosicionRepository.saveAll(ordenadas);
    }

    private int puntosPorPosicion(int posicion) {
        if (posicion < 1 || posicion > PUNTOS_POR_POSICION.size()) {
            return 0;
        }
        return PUNTOS_POR_POSICION.get(posicion - 1);
    }

    private TablaPosicionResponse toTablaPosicion(CampeonatoPosicion posicion) {
        return new TablaPosicionResponse(
                posicion.getUsuario().getId(),
                posicion.getUsuario().getNombrePiloto(),
                posicion.getPuntos(),
                posicion.getPosicion());
    }

    private CampeonatoResponse toResponse(Campeonato campeonato) {
        return new CampeonatoResponse(
                campeonato.getId(),
                campeonato.getNombre(),
                campeonato.getTemporada(),
                campeonato.getCategoria().getId(),
                campeonato.getCategoria().getNombre(),
                campeonato.getEstado(),
                campeonato.getSistemaPuntos());
    }
}
