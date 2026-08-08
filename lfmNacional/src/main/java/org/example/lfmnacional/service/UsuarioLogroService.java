package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.logro.UsuarioLogroResponse;
import org.example.lfmnacional.entity.UsuarioLogro;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.UsuarioLogroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioLogroService {

    private final UsuarioLogroRepository usuarioLogroRepository;

    @Transactional(readOnly = true)
    public List<UsuarioLogroResponse> listarPorUsuario(Long usuarioId) {
        return usuarioLogroRepository.findByUsuario_IdOrderByLogro_Id(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioLogroResponse> listarObtenidos(Long usuarioId) {
        return usuarioLogroRepository.findByUsuario_IdAndObtenidoTrue(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioLogroResponse obtenerProgreso(Long usuarioId, Long logroId) {
        UsuarioLogro usuarioLogro = usuarioLogroRepository
                .findByLogro_IdAndUsuario_Id(logroId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario " + usuarioId + " no tiene el logro " + logroId));
        return toResponse(usuarioLogro);
    }

    private UsuarioLogroResponse toResponse(UsuarioLogro usuarioLogro) {
        return new UsuarioLogroResponse(
                usuarioLogro.getLogro().getId(),
                usuarioLogro.getLogro().getNombre(),
                usuarioLogro.getLogro().getDescripcion(),
                usuarioLogro.getLogro().getTipoCondicion(),
                usuarioLogro.getLogro().getValorCondicion(),
                usuarioLogro.getProgreso(),
                usuarioLogro.getObtenido(),
                usuarioLogro.getFechaObtencion());
    }
}
