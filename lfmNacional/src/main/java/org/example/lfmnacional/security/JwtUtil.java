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
        return generarToken(usuario.getEmail(), claims, expiracionMinutos, usuario.getVersionTokens());
    }

    public String generarToken(String subject, Map<String, Object> claims, long minutos) {
        return generarToken(subject, claims, minutos, 0);
    }

    public String generarToken(String subject, Map<String, Object> claims, long minutos, int tokenVersion) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + minutos * 60_000L);
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(ahora)
                .expiration(expiracion);
        claims.forEach(builder::claim);
        return builder
                .claim("tv", tokenVersion)
                .signWith(secretKey)
                .compact();
    }

    public Long extraerUserId(String token, String scopeEsperado) {
        return Long.valueOf(extraerSubject(token, scopeEsperado));
    }

    /** Subject de un token de proposito unico, exigiendo que su scope sea el esperado. */
    public String extraerSubject(String token, String scopeEsperado) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Token de estado invalido");
        }
        Claims claims = parseClaims(token);
        String scope = claims.get("scope", String.class);
        if (!scopeEsperado.equals(scope)) {
            throw new JwtException("Scope de estado invalido");
        }
        return claims.getSubject();
    }

    public void validarState(String token, String scopeEsperado) {
        if (token == null || token.isBlank()) {
            throw new JwtException("Token de estado invalido");
        }
        Claims claims = parseClaims(token);
        String scope = claims.get("scope", String.class);
        if (!scopeEsperado.equals(scope)) {
            throw new JwtException("Scope de estado invalido");
        }
    }

    /**
     * Verifica firma y expiracion (jjwt lanza {@link JwtException} si falla) y
     * devuelve los claims. Parsear una vez y leer todo de aca: antes el filtro
     * verificaba la firma tres veces por request.
     */
    public Claims parsear(String token) {
        return parseClaims(token);
    }

    /** true si es un token de sesion y no uno de proposito unico (reset, Steam). */
    public static boolean esTokenDeSesion(Claims claims) {
        return claims.get("scope") == null;
    }

    public static int tokenVersion(Claims claims) {
        Object tv = claims.get("tv");
        return tv == null ? 0 : Integer.parseInt(String.valueOf(tv));
    }

    public int extraerTokenVersion(String token) {
        return tokenVersion(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
