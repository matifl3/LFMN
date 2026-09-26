package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.*;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.ApelacionRepository;
import org.example.lfmnacional.repository.CampeonatoMiembroRepository;
import org.example.lfmnacional.repository.CampeonatoPosicionRepository;
import org.example.lfmnacional.repository.CampeonatoRepository;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.IncidentePilotoRepository;
import org.example.lfmnacional.repository.IncidenteRepository;
import org.example.lfmnacional.repository.InscripcionRepository;
import org.example.lfmnacional.repository.NotificacionRepository;
import org.example.lfmnacional.repository.ResolucionIncidenteRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.SancionRepository;
import org.example.lfmnacional.repository.SesionClasificacionRepository;
import org.example.lfmnacional.repository.SetupCalificacionRepository;
import org.example.lfmnacional.repository.SetupComentarioRepository;
import org.example.lfmnacional.repository.SetupRepository;
import org.example.lfmnacional.repository.UsuarioLogroRepository;
import org.example.lfmnacional.repository.UsuarioRecompensaRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
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
    private final CampeonatoMiembroRepository campeonatoMiembroRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final ApelacionRepository apelacionRepository;
    private final CampeonatoPosicionRepository campeonatoPosicionRepository;
    private final IncidentePilotoRepository incidentePilotoRepository;
    private final IncidenteRepository incidenteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final NotificacionRepository notificacionRepository;
    private final ResolucionIncidenteRepository resolucionIncidenteRepository;
    private final SancionRepository sancionRepository;
    private final SesionClasificacionRepository sesionClasificacionRepository;
    private final SetupCalificacionRepository setupCalificacionRepository;
    private final SetupComentarioRepository setupComentarioRepository;
    private final SetupRepository setupRepository;
    private final UsuarioLogroRepository usuarioLogroRepository;
    private final UsuarioRecompensaRepository usuarioRecompensaRepository;
    private final VotoComisarioRepository votoComisarioRepository;
    private final VueltaRepository vueltaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombrePiloto(request.nombrePiloto())
                .passwordEstablecida(true)
                .build();
        usuario = usuarioRepository.save(usuario);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
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
                .passwordEstablecida(false)
                .build();
        usuario = usuarioRepository.save(usuario);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
    }

    @Transactional
    public LoginResponse vincularSteamConLogin(String email, String password, String guidSteam) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("No existe una cuenta con ese email"));
        if (!usuario.isHabilitado()) {
            throw new BusinessException("El usuario esta deshabilitado. Contacta a un administrador.");
        }
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BusinessException("Email o contrasena invalidos");
        }
        if (usuarioRepository.existsByGuidSteam(guidSteam)) {
            throw new BusinessException("Esa cuenta de Steam ya esta vinculada a otro usuario");
        }
        usuario.setGuidSteam(guidSteam);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuarioRepository.save(usuario)));
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Email o contrasena invalidos"));
        if (!usuario.isHabilitado()) {
            throw new BusinessException("El usuario esta deshabilitado. Contacta a un administrador.");
        }
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
        usuario.setGuidSteam(request.guidSteam());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void updatePassword(Long id, CambioPasswordRequest request) {
        Usuario usuario = getEntity(id);
        if (usuario.isPasswordEstablecida() && !passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BusinessException("La contrasena actual es incorrecta");
        }
        if (request.nuevaPassword() == null || request.nuevaPassword().length() < MIN_LENGTH_PASSWORD) {
            throw new BusinessException("La nueva contrasena debe tener al menos " + MIN_LENGTH_PASSWORD + " caracteres");
        }
        usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuario.setPasswordEstablecida(true);
        usuario.setTokenVersion((usuario.getTokenVersion() != null ? usuario.getTokenVersion() : 0) + 1);
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
    public UsuarioResponse updateHabilitado(Long id, boolean habilitado) {
        Usuario usuario = getEntity(id);
        usuario.setHabilitado(habilitado);
        if (!habilitado) {
            usuario.setTokenVersion((usuario.getTokenVersion() != null ? usuario.getTokenVersion() : 0) + 1);
        }
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
        getEntity(id);
        // Referencias directas hacia el usuario
        votoComisarioRepository.deleteByComisario_Id(id);
        resolucionIncidenteRepository.deleteByComisario_Id(id);
        apelacionRepository.deleteByUsuario_Id(id);
        incidentePilotoRepository.deleteByUsuario_Id(id);
        setupComentarioRepository.deleteByUsuario_Id(id);
        setupCalificacionRepository.deleteByUsuario_Id(id);
        // Dependencias de los agregados del usuario (antes de borrarlos)
        apelacionRepository.deleteBySancionUsuarioId(id);
        apelacionRepository.deleteBySancionResolucionIncidenteReportanteId(id);
        sancionRepository.deleteByResolucionIncidenteReportanteId(id);
        votoComisarioRepository.deleteByIncidenteReportanteId(id);
        resolucionIncidenteRepository.deleteByIncidenteReportanteId(id);
        incidentePilotoRepository.deleteByIncidenteReportanteId(id);
        setupComentarioRepository.deleteBySetupAutorId(id);
        setupCalificacionRepository.deleteBySetupAutorId(id);
        // Agregados del usuario
        sancionRepository.deleteByUsuario_Id(id);
        incidenteRepository.deleteByReportante_Id(id);
        setupRepository.deleteByAutor_Id(id);
        // Filas propias sin dependencias
        inscripcionRepository.deleteByUsuario_Id(id);
        notificacionRepository.deleteByUsuario_Id(id);
        resultadoCarreraRepository.deleteByUsuario_Id(id);
        vueltaRepository.deleteByUsuario_Id(id);
        sesionClasificacionRepository.deleteByUsuario_Id(id);
        campeonatoMiembroRepository.deleteByUsuario_Id(id);
        campeonatoPosicionRepository.deleteByUsuario_Id(id);
        eloSancionRepository.deleteByUsuario_Id(id);
        safetyRatingSancionRepository.deleteByUsuario_Id(id);
        usuarioLogroRepository.deleteByUsuario_Id(id);
        usuarioRecompensaRepository.deleteByUsuario_Id(id);
        // Si era dueno de campeonato privado lo deja sin dueno en vez de borrarle
        // los campeonato: la FK de campeonato.admin_id no puede quedar colgando.
        campeonatoRepository.desvincularAdmin(id);
        usuarioRepository.deleteById(id);
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
                usuario.getFechaRegistro(),
                usuario.isPasswordEstablecida(),
                usuario.isHabilitado());
    }
}
