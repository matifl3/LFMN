package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.anuncio.AnuncioRequest;
import org.example.lfmnacional.dto.anuncio.AnuncioResponse;
import org.example.lfmnacional.entity.Anuncio;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.AnuncioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnuncioService {
    private final AnuncioRepository anuncioRepository;

    public Anuncio getEntity(Long id) {
        return anuncioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anuncio no encontrado con id " + id));
    }

    public AnuncioResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Cacheable("anuncios")
    public List<AnuncioResponse> listAll() {
        return anuncioRepository.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    @Cacheable("anuncio_ultimo")
    public AnuncioResponse getUltimo() {
        Anuncio anuncio = anuncioRepository.findFirstByDestacadoTrueOrderByFechaDesc()
                .orElseGet(() -> anuncioRepository.findFirstByOrderByFechaDesc()
                        .orElseThrow(() -> new ResourceNotFoundException("No hay anuncios registrados")));
        return toResponse(anuncio);
    }

    @Transactional
    @CacheEvict(value = {"anuncio_ultimo", "anuncios"}, allEntries = true)
    public AnuncioResponse create(AnuncioRequest request) {
        Anuncio anuncio = Anuncio.builder()
                .titulo(request.titulo())
                .contenido(request.contenido())
                .urlImagen(request.urlImagen())
                .build();
        return toResponse(anuncioRepository.save(anuncio));
    }

    @Transactional
    @CacheEvict(value = {"anuncio_ultimo", "anuncios"}, allEntries = true)
    public void delete(Long id) {
        anuncioRepository.delete(getEntity(id));
    }

    @Transactional
    @CacheEvict(value = {"anuncio_ultimo", "anuncios"}, allEntries = true)
    public AnuncioResponse toggleDestacado(Long id) {
        Anuncio anuncio = getEntity(id);
        if (Boolean.TRUE.equals(anuncio.getDestacado())) {
            anuncio.setDestacado(false);
        } else {
            anuncioRepository.findFirstByDestacadoTrueOrderByFechaDesc()
                    .ifPresent(a -> a.setDestacado(false));
            anuncio.setDestacado(true);
        }
        return toResponse(anuncioRepository.save(anuncio));
    }

    private AnuncioResponse toResponse(Anuncio anuncio) {
        return new AnuncioResponse(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getContenido(),
                anuncio.getUrlImagen(),
                anuncio.getFecha(),
                anuncio.getDestacado());
    }
}
