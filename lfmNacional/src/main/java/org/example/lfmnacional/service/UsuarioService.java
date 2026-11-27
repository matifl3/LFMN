package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.CambioPasswordRequest;
import org.example.lfmnacional.dto.usuario.EloHistorialResponse;
import org.example.lfmnacional.dto.usuario.RatingRequest;
import org.example.lfmnacional.dto.usuario.SafetyRatingHistorialResponse;
import org.example.lfmnacional.dto.usuario.StatsResponse;
import org.example.lfmnacional.dto.usuario.UsuarioBasicoResponse;
import org.example.lfmnacional.dto.usuario.UsuarioRequest;
import org.example.lfmnacional.dto.usuario.UsuarioResponse;
import org.example.lfmnacional.entity.EloSancion;
import org.example.lfmnacional.entity.SafetyRatingSancion;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.enums.Rol;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.mapper.UsuarioMapper;
import org.example.lfmnacional.repository.EloSancionRepository;
import org.example.lfmnacional.repository.ResultadoCarreraRepository;
import org.example.lfmnacional.repository.ResumenResultados;
import org.example.lfmnacional.repository.SafetyRatingSancionRepository;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * El usuario como piloto: perfil, password, rol/habilitado/rating (admin),
 * estadisticas e historiales. Registro y login estan en
 * {@link AutenticacionService}; el borrado en cascada, en {@link BajaUsuarioService}.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EloSancionRepository eloSancionRepository;
    private final SafetyRatingSancionRepository safetyRatingSancionRepository;
    private final ResultadoCarreraRepository resultadoCarreraRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampeonatoAccesoService accesoService;

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
        boolean completo = accesoService.esPropio(visor, id) || accesoService.esAdminGlobal(visor);
        return completo ? UsuarioMapper.completo(usuario) : UsuarioMapper.publico(usuario);
    }

    public UsuarioResponse me(Usuario usuario) {
        return UsuarioMapper.completo(usuario);
    }

    public List<UsuarioResponse> listAll() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::completo).toList();
    }

    @Cacheable("usuarios")
    public List<UsuarioBasicoResponse> listAllBasico() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::basico).toList();
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponse updatePerfil(Long id, UsuarioRequest request, Usuario actor) {
        accesoService.exigirPropioOAdmin(actor, id, "editar el perfil de otro usuario");
        Usuario usuario = getEntity(id);
        boolean cambiaEmail = !usuario.getEmail().equalsIgnoreCase(request.email());
        // El email es la llave de la recuperacion de password: cambiarlo exige la
        // password actual, salvo que lo haga un ADMIN sobre otro usuario o que la
        // cuenta (Steam) todavia no tenga password propia.
        boolean editaSuPropioPerfil = accesoService.esPropio(actor, id);
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
        return UsuarioMapper.completo(usuarioRepository.save(usuario));
    }

    @Transactional
    public void updatePassword(Long id, CambioPasswordRequest request, Usuario actor) {
        accesoService.exigirPropio(actor, id, "cambiar la password de otro usuario");
        Usuario usuario = getEntity(id);
        if (usuario.isPasswordEstablecida() && !passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BusinessException("La contrasena actual es incorrecta");
        }
        Usuario.validarPasswordNueva(request.nuevaPassword());
        usuario.establecerPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse desvincularSteam(Long id, Usuario actor) {
        accesoService.exigirPropio(actor, id, "desvincular Steam de otro usuario");
        Usuario usuario = getEntity(id);
        usuario.setGuidSteam(null);
        return UsuarioMapper.completo(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol rol) {
        Usuario usuario = getEntity(id);
        usuario.setRol(rol);
        return UsuarioMapper.completo(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse updateHabilitado(Long id, boolean habilitado) {
        Usuario usuario = getEntity(id);
        usuario.setHabilitado(habilitado);
        if (!habilitado) {
            usuario.invalidarTokens();
        }
        return UsuarioMapper.completo(usuarioRepository.save(usuario));
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
        return UsuarioMapper.completo(usuarioRepository.save(usuario));
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
}
