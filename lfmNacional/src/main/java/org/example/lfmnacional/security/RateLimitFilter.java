package org.example.lfmnacional.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_INTENTOS = 5;
    private static final long VENTANA_MILIS = 60_000;
    private static final long LOCKOUT_MILIS = 300_000;

    private final Map<String, IntentoCuenta> intentosPorIp = new ConcurrentHashMap<>();

    private record IntentoCuenta(LinkedList<Long> marcas, long bloqueadoHasta) {

        static IntentoCuenta vacia() {
            return new IntentoCuenta(new LinkedList<>(), 0);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (esEndpointProtegido(request)) {
            if (excedeLimite(obtenerIp(request))) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("{\"error\":\"TOO_MANY_REQUESTS\",\"mensaje\":\"Demasiados intentos. Intentá de nuevo en unos minutos\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean esEndpointProtegido(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return "/api/usuarios/login".equals(uri) || "/api/usuarios/registro-steam".equals(uri);
    }

    private boolean excedeLimite(String ip) {
        long ahora = System.currentTimeMillis();
        IntentoCuenta cuenta = intentosPorIp.computeIfAbsent(ip, k -> IntentoCuenta.vacia());
        synchronized (cuenta) {
            if (ahora < cuenta.bloqueadoHasta()) {
                return true;
            }
            while (!cuenta.marcas().isEmpty() && ahora - cuenta.marcas().peek() > VENTANA_MILIS) {
                cuenta.marcas().poll();
            }
            if (cuenta.marcas().size() >= MAX_INTENTOS) {
                cuenta = new IntentoCuenta(new LinkedList<>(), ahora + LOCKOUT_MILIS);
                intentosPorIp.put(ip, cuenta);
                return true;
            }
            cuenta.marcas().offerLast(ahora);
            return false;
        }
    }

    private String obtenerIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}