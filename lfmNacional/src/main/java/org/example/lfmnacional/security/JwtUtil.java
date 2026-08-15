package org.example.lfmnacional.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.lfmnacional.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiracionMinutos;

    public JwtUtil(@Value("${jwt.secreto}") String secreto,
                   @Value("${jwt.expiracion-minutos}") long expiracionMinutos) {
        this.secretKey = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("nombrePiloto", usuario.getNombrePiloto());
        return generarToken(usuario.getEmail(), claims, expiracionMinutos);
    }

    public String generarToken(String subject, Map<String, Object> claims, long minutos) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + minutos * 60_000L);
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(ahora)
                .expiration(expiracion);
        claims.forEach(builder::claim);
        return builder
                .signWith(secretKey)
                .compact();
    }

    public Long extraerUserId(String token, String scopeEsperado) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Token de estado invalido");
        }
        Claims claims = parseClaims(token);
        String scope = claims.get("scope", String.class);
        if (!scopeEsperado.equals(scope)) {
            throw new JwtException("Scope de estado invalido");
        }
        return Long.valueOf(claims.getSubject());
    }

    public String extraerEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean esValido(String token, String email) {
        Claims claims = parseClaims(token);
        return email.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
