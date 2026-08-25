package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.RecompensaRequest;
import org.example.lfmnacional.dto.logro.RecompensaResponse;
import org.example.lfmnacional.dto.logro.UsuarioRecompensaResponse;
import org.example.lfmnacional.entity.Recompensa;
import org.example.lfmnacional.entity.UsuarioRecompensa;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.RecompensaRepository;
import org.example.lfmnacional.repository.UsuarioRecompensaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final UsuarioRecompensaRepository usuarioRecompensaRepository;
    private final LogroService logroService;
    private final UsuarioService usuarioService;

    public Recompensa getEntity(Long id) {
        return recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "recompensas", key = "#id")
    public RecompensaResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable("recompensas")
    public List<RecompensaResponse> listAll() {
        return recompensaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RecompensaResponse> listarPorLogro(Long logroId) {
        return recompensaRepository.findByLogro_Id(logroId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = "recompensas", allEntries = true)
    public RecompensaResponse create(Long logroId, RecompensaRequest request) {
        Recompensa recompensa = Recompensa.builder()
                .logro(logroService.getEntity(logroId))
                .descripcion(request.descripcion())
                .tipo(request.tipo())
                .build();
        return toResponse(recompensaRepository.save(recompensa));
    }

    @Transactional
    @CacheEvict(value = "recompensas", allEntries = true)
    public RecompensaResponse update(Long id, RecompensaRequest request) {
        Recompensa recompensa = getEntity(id);
        recompensa.setDescripcion(request.descripcion());
        recompensa.setTipo(request.tipo());
        return toResponse(recompensaRepository.save(recompensa));
    }

    @Transactional
    @CacheEvict(value = "recompensas", allEntries = true)
    public void delete(Long id) {
        usuarioRecompensaRepository.deleteByRecompensa_Id(id);
        recompensaRepository.delete(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioRecompensaResponse> listarRecompensasUsuario(Long usuarioId) {
        usuarioService.getEntity(usuarioId);
        return usuarioRecompensaRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::toUsuarioRecompensaResponse)
                .toList();
    }

    @Transactional
    public UsuarioRecompensaResponse reclamarRecompensa(Long usuarioId, Long recompensaId) {
        usuarioService.getEntity(usuarioId);
        recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con id " + recompensaId));
        UsuarioRecompensa usuarioRecompensa = usuarioRecompensaRepository
                .findByRecompensa_IdAndUsuario_Id(recompensaId, usuarioId)
                .orElseThrow(() -> new BusinessException("Este usuario no tiene esa recompensa"));
        usuarioRecompensa.setReclamada(true);
        return toUsuarioRecompensaResponse(usuarioRecompensaRepository.save(usuarioRecompensa));
    }

    private RecompensaResponse toResponse(Recompensa recompensa) {
        return new RecompensaResponse(
                recompensa.getId(),
                recompensa.getLogro().getId(),
                recompensa.getDescripcion(),
                recompensa.getTipo());
    }

    private UsuarioRecompensaResponse toUsuarioRecompensaResponse(UsuarioRecompensa usuarioRecompensa) {
        Recompensa recompensa = usuarioRecompensa.getRecompensa();
        return new UsuarioRecompensaResponse(
                recompensa.getId(),
                recompensa.getLogro().getId(),
                recompensa.getDescripcion(),
                recompensa.getTipo(),
                usuarioRecompensa.getReclamada(),
                usuarioRecompensa.getFecha());
    }
}
