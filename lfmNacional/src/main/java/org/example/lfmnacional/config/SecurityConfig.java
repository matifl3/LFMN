package org.example.lfmnacional.config;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints (public)
                        .requestMatchers("/api/usuarios/registro", "/api/usuarios/login", "/api/usuarios/registro-steam").permitAll()
                        // Steam OAuth flow (public)
                        .requestMatchers("/api/steam/**").permitAll()
                        // Public read-only endpoints
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campeonatos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/carreras/proximas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/carreras/pasadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/carreras").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/carreras/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}/historial-elo").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}/historial-safety-rating").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}/logros/obtenidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/anuncios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/logros/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/carrera/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/carrera/{id}/count").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resultados/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clasificaciones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/setups").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/setups/{id}").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"mensaje\":\"Debes iniciar sesion\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("{\"error\":\"FORBIDDEN\",\"mensaje\":\"No tienes permisos para esta accion\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
