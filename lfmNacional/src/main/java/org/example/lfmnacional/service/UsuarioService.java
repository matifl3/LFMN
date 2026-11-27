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
import org.example.lfmnacional.repository.ResumenResultados;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.repository.VotoComisarioRepository;
import org.example.lfmnacional.repository.VueltaRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.example.lfmnacional.security.LoginIntentos;
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
    private final SteamService steamService;
    private final LoginIntentos loginIntentos;

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
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
    @CacheEvict(value = "usuarios", allEntries = true)
    public LoginResponse registrarSteam(SteamRegistroRequest request) {
        String guidSteam = steamService.extraerGuidRegistro(request.tokenRegistro());
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (usuarioRepository.existsByGuidSteam(guidSteam)) {
            throw new BusinessException("Ya existe un usuario vinculado a esa cuenta de Steam");
        }
        if (usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombrePiloto(request.nombrePiloto())
                .guidSteam(guidSteam)
                .passwordEstablecida(false)
                .build();
        usuario = usuarioRepository.save(usuario);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
    }

    @Transactional
    public LoginResponse vincularSteamConLogin(String email, String password, String tokenRegistro) {
        String guidSteam = steamService.extraerGuidRegistro(tokenRegistro);
        Usuario usuario = autenticar(email, password);
        if (usuarioRepository.existsByGuidSteam(guidSteam)) {
            throw new BusinessException("Esa cuenta de Steam ya esta vinculada a otro usuario");
        }
        usuario.setGuidSteam(guidSteam);
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuarioRepository.save(usuario)));
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = autenticar(request.email(), request.password());
        return new LoginResponse(jwtUtil.generarToken(usuario), toResponse(usuario));
    }

    /**
     * Valida email + password con bloqueo por cuenta tras varios fallos. El mismo
     * mensaje para email inexistente o password incorrecta, y el aviso de cuenta
     * deshabilitada solo despues de una password correcta: no se puede usar para
     * averiguar que emails existen.
     */
    private Usuario autenticar(String email, String password) {
        if (loginIntentos.bloqueado(email)) {
            throw new BusinessException("Demasiados intentos fallidos. Proba de nuevo en 15 minutos");
        }
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            loginIntentos.registrarFallo(email);
            throw new BusinessException("Email o contrasena invalidos");
        }
        loginIntentos.registrarExito(email);
        if (!usuario.isHabilitado()) {
            throw new BusinessException("El usuario esta deshabilitado. Contacta a un administrador.");
        }
        return usuario;
    }

    public Usuario getEntity(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    /**
     * Perfil por id. El endpoint es publico (perfil de piloto), asi que solo el
     * propio usuario o un ADMIN reciben los datos privados (email, GUID de Steam,
     * estado de la password); el resto ve la version publica.
     */
    public UsuarioResponse getById(Long id, Usuario visor) {
        Usuario usuario = getEntity(id);
        boolean completo = visor != null
                && (visor.getId().equals(id) || visor.getRol() == Rol.ADMIN);
        return completo ? toResponse(usuario) : toResponsePublico(usuario);
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
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponse updatePerfil(Long id, UsuarioRequest request, Usuario actor) {
        Usuario usuario = getEntity(id);
        boolean cambiaEmail = !usuario.getEmail().equalsIgnoreCase(request.email());
        // El email es la llave de la recuperacion de password: cambiarlo exige la
        // password actual, salvo que lo haga un ADMIN sobre otro usuario o que la
        // cuenta (Steam) todavia no tenga password propia.
        boolean editaSuPropioPerfil = actor != null && actor.getId().equals(id);
        if (cambiaEmail && editaSuPropioPerfil && usuario.isPasswordEstablecida()
                && (request.password() == null
                    || !passwordEncoder.matches(request.password(), usuario.getPassword()))) {
            throw new BusinessException("Para cambiar el email ingresa tu contrasena actual");
        }
        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + request.email());
        }
        if (request.nombrePiloto() != null && !request.nombrePiloto().isBlank()
                && !request.nombrePiloto().equals(usuario.getNombrePiloto())
                && usuarioRepository.existsByNombrePiloto(request.nombrePiloto())) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
        usuario.setEmail(request.email());
        usuario.setNombrePiloto(request.nombrePiloto());
        // guidSteam NO se toca aca: se gestiona solo via OpenID (/api/steam) y DELETE /{id}/steam.
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void updatePassword(Long id, CambioPasswordRequest request) {
        Usuario usuario = getEntity(id);
        if (usuario.isPasswordEstablecida() && !passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BusinessException("La contrasena actual es incorrecta");
        }
        Usuario.validarPasswordNueva(request.nuevaPassword());
        usuario.establecerPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(usuario);
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
            usuario.invalidarTokens();
        }
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
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
        ResumenResultados r = resultadoCarreraRepository.resumenPorUsuario(id);
        long vueltasRapidas = resultadoCarreraRepository.countVueltaRapidaByUsuario(id);
        double porcentaje = r.carreras() == 0 ? 0.0
                : (double) r.finalizadas() / r.carreras() * 100;
        return new StatsResponse(r.carreras(), r.victorias(), r.podios(), r.poles(), vueltasRapidas, porcentaje);
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

    private UsuarioResponse toResponsePublico(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                null,
                usuario.getNombrePiloto(),
                usuario.getFotoPerfil(),
                null,
                usuario.getElo(),
                usuario.getSafetyRating(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                null,
                usuario.isHabilitado());
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
