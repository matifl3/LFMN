package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.dto.usuario.LoginRequest;
import org.example.lfmnacional.dto.usuario.LoginResponse;
import org.example.lfmnacional.dto.usuario.RegistroRequest;
import org.example.lfmnacional.dto.usuario.SteamRegistroRequest;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.mapper.UsuarioMapper;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.example.lfmnacional.security.LoginIntentos;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Entrar al sistema: registro (email o Steam), login y vincular Steam a una
 * cuenta existente. Se separo de {@code UsuarioService} (GRASP Alta cohesion):
 * cambia por razones de seguridad (JWT, bloqueo por intentos, OpenID), no por
 * reglas del perfil.
 */
@Service
@RequiredArgsConstructor
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SteamService steamService;
    private final LoginIntentos loginIntentos;

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public LoginResponse registrar(RegistroRequest request) {
        exigirEmailYNombreLibres(request.email(), request.nombrePiloto());
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nombrePiloto(request.nombrePiloto())
                .passwordEstablecida(true)
                .build());
        return sesion(usuario);
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
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombrePiloto(request.nombrePiloto())
                .guidSteam(guidSteam)
                .passwordEstablecida(false)
                .build());
        return sesion(usuario);
    }

    @Transactional
    public LoginResponse vincularSteamConLogin(String email, String password, String tokenRegistro) {
        String guidSteam = steamService.extraerGuidRegistro(tokenRegistro);
        Usuario usuario = autenticar(email, password);
        if (usuarioRepository.existsByGuidSteam(guidSteam)) {
            throw new BusinessException("Esa cuenta de Steam ya esta vinculada a otro usuario");
        }
        usuario.setGuidSteam(guidSteam);
        return sesion(usuarioRepository.save(usuario));
    }

    public LoginResponse login(LoginRequest request) {
        return sesion(autenticar(request.email(), request.password()));
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

    private void exigirEmailYNombreLibres(String email, String nombrePiloto) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe un usuario con el email " + email);
        }
        if (usuarioRepository.existsByNombrePiloto(nombrePiloto)) {
            throw new BusinessException("Ya existe un usuario con ese nombre de piloto");
        }
    }

    private LoginResponse sesion(Usuario usuario) {
        return new LoginResponse(jwtUtil.generarToken(usuario), UsuarioMapper.completo(usuario));
    }
}
