package org.example.lfmnacional.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.exception.BusinessException;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperarPasswordService {

    private static final String SCOPE_RESET = "reset";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.recovery.from}")
    private String from;

    @Value("${app.recovery.expira-minutos}")
    private long expiraMinutos;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public void solicitar(String email) {
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            // Lleva el tokenVersion actual: al restablecer se incrementa, asi que el
            // link sirve una sola vez (y cualquier cambio de password lo invalida).
            String token = jwtUtil.generarToken(
                    String.valueOf(u.getId()),
                    Map.of("scope", SCOPE_RESET),
                    expiraMinutos,
                    u.getVersionTokens());
            String link = frontendUrl + "/recuperar?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);
            JavaMailSender sender = mailSenderProvider.getIfAvailable();
            if (mailHost == null || mailHost.isBlank() || sender == null) {
                log.warn("SMTP no configurado (MAIL_HOST vacío). Link de recuperación para {}: {}", u.getEmail(), link);
                return;
            }
            try {
                MimeMessage msg = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
                helper.setFrom(from);
                helper.setTo(u.getEmail());
                helper.setSubject("Recuperación de contraseña — LFM Nacional");
                helper.setText(mensaje(link), true);
                sender.send(msg);
                log.info("Enlace de recuperación enviado a {}", u.getEmail());
            } catch (MessagingException e) {
                log.error("Error preparando el correo de recuperación", e);
            } catch (MailException e) {
                log.warn("No se pudo enviar el correo (host={}). Link de recuperación: {}", mailHost, link);
            }
        });
    }

    @Transactional
    public void restablecer(String token, String nuevaPassword) {
        Long userId;
        int versionToken;
        try {
            userId = jwtUtil.extraerUserId(token, SCOPE_RESET);
            versionToken = jwtUtil.extraerTokenVersion(token);
        } catch (Exception e) {
            throw new BusinessException("El enlace de recuperación es inválido o expiró");
        }
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("El enlace de recuperación es inválido o expiró"));
        if (versionToken != usuario.getVersionTokens()) {
            throw new BusinessException("El enlace de recuperación ya fue usado o expiró");
        }
        Usuario.validarPasswordNueva(nuevaPassword);
        usuario.establecerPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        log.info("Contraseña restablecida para el usuario {}", usuario.getId());
    }

    private String mensaje(String link) {
        return "<p>Recibimos una solicitud para restablecer tu contraseña de <strong>LFM Nacional</strong>.</p>"
                + "<p>Ingresá al siguiente enlace para crear una nueva contraseña "
                + "(vence en " + expiraMinutos + " minutos):</p>"
                + "<p><a href=\"" + link + "\">Restablecer contraseña</a></p>"
                + "<p style=\"color:#777\">Si no solicitaste este cambio, podés ignorar este correo.</p>";
    }
}