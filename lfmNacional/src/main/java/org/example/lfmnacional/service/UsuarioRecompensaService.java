package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.UsuarioRecompensaResponse;
import org.example.lfmnacional.entity.UsuarioRecompensa;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.UsuarioRecompensaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRecompensaService {

    private final UsuarioRecompensaRepository usuarioRecompensaRepository;

    @Transactional(readOnly = true)
    public List<UsuarioRecompensaResponse> listarPorUsuario(Long usuarioId) {
        return usuarioRecompensaRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioRecompensaResponse> listarNoReclamadas(Long usuarioId) {
        return usuarioRecompensaRepository.findByUsuario_IdAndReclamadaFalse(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioRecompensaResponse getById(Long id) {
        UsuarioRecompensa usuarioRecompensa = usuarioRecompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa de usuario no encontrada con id " + id));
        return toResponse(usuarioRecompensa);
    }

    private UsuarioRecompensaResponse toResponse(UsuarioRecompensa usuarioRecompensa) {
        return new UsuarioRecompensaResponse(
                usuarioRecompensa.getRecompensa().getId(),
                usuarioRecompensa.getRecompensa().getLogro().getId(),
                usuarioRecompensa.getRecompensa().getDescripcion(),
                usuarioRecompensa.getRecompensa().getTipo(),
                usuarioRecompensa.getReclamada(),
                usuarioRecompensa.getFecha());
    }
}
