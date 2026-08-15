package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.categoria.CategoriaRequest;
import org.example.lfmnacional.dto.categoria.CategoriaResponse;
import org.example.lfmnacional.entity.Categoria;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public Categoria getEntity(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id " + id));
    }

    public CategoriaResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public List<CategoriaResponse> listAll() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<CategoriaResponse> disponiblesPorElo(Integer elo) {
        return categoriaRepository
                .findByEloMinimoLessThanEqualAndEloMaximoGreaterThanEqual(elo, elo)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.nombre())) {
            throw new BusinessException("Ya existe una categoria con el nombre " + request.nombre());
        }
        validarRangoElo(request);
        Categoria categoria = Categoria.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .eloMinimo(request.eloMinimo())
                .eloMaximo(request.eloMaximo())
                .setupAbierto(request.setupAbierto())
                .setupFijo(request.setupFijo())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse update(Long id, CategoriaRequest request) {
        validarRangoElo(request);
        Categoria categoria = getEntity(id);
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        categoria.setEloMinimo(request.eloMinimo());
        categoria.setEloMaximo(request.eloMaximo());
        categoria.setSetupAbierto(request.setupAbierto());
        categoria.setSetupFijo(request.setupFijo());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long id) {
        categoriaRepository.delete(getEntity(id));
    }

    private void validarRangoElo(CategoriaRequest request) {
        if (request.eloMinimo() != null && request.eloMaximo() != null
                && request.eloMaximo() < request.eloMinimo()) {
            throw new BusinessException("El elo maximo no puede ser menor al elo minimo");
        }
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEloMinimo(),
                categoria.getEloMaximo(),
                categoria.getSetupAbierto(),
                categoria.getSetupFijo());
    }
}
