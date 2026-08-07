package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.anuncio.AnuncioResponse;
import org.example.lfmnacional.entity.Anuncio;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.AnuncioRepository;
import org.springframework.stereotype.Service;

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

    public List<AnuncioResponse> listAll() {
        return anuncioRepository.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    public AnuncioResponse getUltimo() {
        return toResponse(anuncioRepository.findFirstByOrderByFechaDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No hay anuncios registrados")));
    }

    private AnuncioResponse toResponse(Anuncio anuncio) {
        return new AnuncioResponse(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getContenido(),
                anuncio.getUrlImagen(),
                anuncio.getFecha());
    }
}
