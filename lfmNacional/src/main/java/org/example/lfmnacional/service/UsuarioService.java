package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.*;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final int MIN_LENGTH_PASSWORD = 6;

    private final UsuarioRepository usuarioRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponse registrar(UsuarioRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("La contrasena es obligatoria");
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (request.guidSteam() != null && usuarioRepository.existsByGuidSteam(request.guidSteam())) {
            throw new BusinessException("Ya existe un usuario vinculado a esa cuenta de Steam");
        }
        if (request.nombrePiloto() != null && !request.nombrePiloto().isBlank()
                && usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombrePiloto(request.nombrePiloto())
                .fotoPerfil(request.fotoPerfil())
                .guidSteam(request.guidSteam())
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public LoginResponse registrarSteam(SteamRegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (usuarioRepository.existsByGuidSteam(request.guidSteam())) {
            throw new BusinessException("Ya existe un usuario vinculado a esa cuenta de Steam");
        }
        if (usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombrePiloto(request.nombrePiloto())
                .guidSteam(request.guidSteam())
                .build();
        usuario = usuarioRepository.save(usuario);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Email o contrasena invalidos"));
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BusinessException("Email o contrasena invalidos");
        }
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
    }

    public Usuario getEntity(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    public UsuarioResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    public UsuarioResponse me(Usuario usuario) {
        return toResponse(usuario);
    }

    public List<UsuarioResponse> listAll() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Cacheable("usuarios")
    public List<UsuarioBasicoResponse> listAllBasico() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioBasicoResponse(
                        u.getId(), u.getNombrePiloto(), u.getFotoPerfil(), u.getElo(), u.getSafetyRating()))
                .toList();
    }

    @Transactional
    public UsuarioResponse updatePerfil(Long id, UsuarioRequest request) {
        Usuario usuario = getEntity(id);
        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (request.guidSteam() != null
                && !request.guidSteam().equals(usuario.getGuidSteam())
                && usuarioRepository.existsByGuidSteam(request.guidSteam())) {
            throw new BusinessException("Ya existe un usuario vinculado a esa cuenta de Steam");
        }
        if (request.nombrePiloto() != null && !request.nombrePiloto().isBlank()
                && !request.nombrePiloto().equals(usuario.getNombrePiloto())
                && usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        usuario.setEmail(request.email());
        usuario.setNombrePiloto(request.nombrePiloto());
        usuario.setFotoPerfil(request.fotoPerfil());
        usuario.setGuidSteam(request.guidSteam());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void updatePassword(Long id, CambioPasswordRequest request) {
        Usuario usuario = getEntity(id);
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BusinessException("La contrasena actual es incorrecta");
        }
        if (request.nuevaPassword().length() < MIN_LENGTH_PASSWORD) {
            throw new BusinessException("La nueva contrasena debe tener al menos " + MIN_LENGTH_PASSWORD + " caracteres");
        }
        usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse vincularSteam(Long id, SteamRequest request) {
        Usuario usuario = getEntity(id);
        if (usuarioRepository.existsByGuidSteam(request.guidSteam())) {
            throw new BusinessException("Ya existe un usuario vinculado a esa cuenta de Steam");
        }
        usuario.setGuidSteam(request.guidSteam());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse desvincularSteam(Long id) {
        Usuario usuario = getEntity(id);
        usuario.setGuidSteam(null);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol rol) {
        Usuario usuario = getEntity(id);
        usuario.setRol(rol);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse updateRating(Long id, RatingRequest request) {
        Usuario usuario = getEntity(id);
        if (request.elo() != null) {
            int deltaElo = request.elo() - usuario.getElo();
            usuario.setElo(request.elo());
            if (deltaElo != 0) {
                eloSancionRepository.save(EloSancion.builder()
                        .usuario(usuario)
                        .cambio(deltaElo)
                        .motivo("Ajuste manual del admin")
                        .build());
            }
        }
        if (request.safetyRating() != null) {
            int deltaSr = request.safetyRating() - usuario.getSafetyRating();
            usuario.setSafetyRating(request.safetyRating());
            if (deltaSr != 0) {
                safetyRatingSancionRepository.save(SafetyRatingSancion.builder()
                        .usuario(usuario)
                        .cambio(deltaSr)
                        .motivo("Ajuste manual del admin")
                        .build());
            }
        }
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void delete(Long id) {
        Usuario usuario = getEntity(id);
        usuarioRepository.delete(usuario);
    }

    public StatsResponse getStats(Long id) {
        long carrerasDisputadas = resultadoCarreraRepository.countByUsuario_Id(id);
        long carrerasFinalizadas = resultadoCarreraRepository.countByUsuario_IdAndFinalizoTrue(id);
        long victorias = resultadoCarreraRepository.countByUsuario_IdAndPosicionFinal(id, 1);
        long podios = resultadoCarreraRepository.countByUsuario_IdAndPosicionFinalLessThanEqual(id, 3);
        long poles = resultadoCarreraRepository.countByUsuario_IdAndPolesTrue(id);
        long vueltasRapidas = resultadoCarreraRepository.countVueltaRapidaByUsuario(id);
        double porcentaje = carrerasDisputadas == 0 ? 0.0
                : (double) carrerasFinalizadas / carrerasDisputadas * 100;
        return new StatsResponse(carrerasDisputadas, victorias, podios, poles, vueltasRapidas, porcentaje);
    }

    @Transactional(readOnly = true)
    public List<EloHistorialResponse> getHistorialElo(Long id) {
        getEntity(id);
        return eloSancionRepository.findByUsuario_IdOrderByFechaDesc(id).stream()
                .map(this::toEloHistorial)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SafetyRatingHistorialResponse> getHistorialSafetyRating(Long id) {
        getEntity(id);
        return safetyRatingSancionRepository.findByUsuario_IdOrderByFechaDesc(id).stream()
                .map(this::toSafetyRatingHistorial)
                .toList();
    }

    private EloHistorialResponse toEloHistorial(EloSancion sancion) {
        return new EloHistorialResponse(
                sancion.getId(),
                sancion.getCambio(),
                sancion.getMotivo(),
                sancion.getFecha(),
                sancion.getCarrera() != null ? sancion.getCarrera().getId() : null);
    }

    private SafetyRatingHistorialResponse toSafetyRatingHistorial(SafetyRatingSancion sancion) {
        return new SafetyRatingHistorialResponse(
                sancion.getId(),
                sancion.getCambio(),
                sancion.getMotivo(),
                sancion.getFecha(),
                sancion.getCarrera() != null ? sancion.getCarrera().getId() : null);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombrePiloto(),
                usuario.getFotoPerfil(),
                usuario.getGuidSteam(),
                usuario.getElo(),
                usuario.getSafetyRating(),
                usuario.getRol(),
                usuario.getFechaRegistro());
    }
}
