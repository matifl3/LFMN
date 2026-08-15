package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.setup.SetupComentarioRequest;
import org.example.lfmnacional.dto.setup.SetupComentarioResponse;
import org.example.lfmnacional.entity.SetupComentario;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetupComentarioService {

    private final SetupComentarioRepository setupComentarioRepository;
    private final SetupService setupService;
    private final UsuarioService usuarioService;

    public SetupComentario getEntity(Long id) {
        return setupComentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public SetupComentarioResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<SetupComentarioResponse> listarPorSetup(Long setupId) {
        return setupComentarioRepository.findBySetup_IdOrderByFechaDesc(setupId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public SetupComentarioResponse create(Long setupId, SetupComentarioRequest request) {
        SetupComentario comentario = SetupComentario.builder()
                .setup(setupService.getEntity(setupId))
                .usuario(usuarioService.getEntity(request.usuarioId()))
                .texto(request.texto())
                .build();
        return toResponse(setupComentarioRepository.save(comentario));
    }

    @Transactional
    public SetupComentarioResponse update(Long id, SetupComentarioRequest request) {
        SetupComentario comentario = getEntity(id);
        comentario.setTexto(request.texto());
        return toResponse(setupComentarioRepository.save(comentario));
    }

    @Transactional
    public void delete(Long id) {
        setupComentarioRepository.delete(getEntity(id));
    }

    private SetupComentarioResponse toResponse(SetupComentario comentario) {
        return new SetupComentarioResponse(
                comentario.getId(),
                comentario.getSetup().getId(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNombrePiloto(),
                comentario.getUsuario().getFotoPerfil(),
                comentario.getTexto(),
                comentario.getFecha());
    }
}
