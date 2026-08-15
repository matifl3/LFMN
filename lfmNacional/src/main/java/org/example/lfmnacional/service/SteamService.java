package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SteamService {

    private static final String STEAM_OPENID_ENDPOINT = "https://steamcommunity.com/openid/login";
    private static final String OPENID_NS = "http://specs.openid.net/auth/2.0";
    private static final String OPENID_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    private static final Pattern STEAM_ID_PATTERN =
            Pattern.compile("^https?://steamcommunity\\.com/openid/id/(\\d{17})$");
    private static final String SCOPE_VINCULACION = "steam-vinculacion";
    private static final long STATE_TIMEOUT_MINUTOS = 10;

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Value("${steam.realm}")
    private String realm;

    @Value("${steam.return-to}")
    private String returnTo;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String generarUrlVinculacion(Long userId) {
        String state = jwtUtil.generarToken(
                String.valueOf(userId),
                Map.of("scope", SCOPE_VINCULACION),
                STATE_TIMEOUT_MINUTOS);
        return UriComponentsBuilder.fromUriString(STEAM_OPENID_ENDPOINT)
                .queryParam("openid.ns", OPENID_NS)
                .queryParam("openid.mode", "checkid_setup")
                .queryParam("openid.return_to", returnTo)
                .queryParam("openid.realm", realm)
                .queryParam("openid.identity", OPENID_SELECT)
                .queryParam("openid.claimed_id", OPENID_SELECT)
                .queryParam("openid.state", state)
                .build()
                .toUriString();
    }

    public String procesarCallback(Map<String, String> params) {
        if (!"id_res".equals(params.get("openid.mode"))) {
            return "invalido";
        }
        if (!returnTo.equals(params.get("openid.return_to"))) {
            return "invalido";
        }
        if (!verificarFirma(params)) {
            return "invalido";
        }

        String identidad = params.get("openid.identity");
        Matcher matcher = identidad != null ? STEAM_ID_PATTERN.matcher(identidad) : null;
        if (matcher == null || !matcher.matches()) {
            return "invalido";
        }
        String guidSteam = matcher.group(1);

        Long userId;
        try {
            userId = jwtUtil.extraerUserId(params.get("openid.state"), SCOPE_VINCULACION);
        } catch (Exception e) {
            return "expirado";
        }

        Usuario usuario = usuarioRepository.findById(userId).orElse(null);
        if (usuario == null) {
            return "invalido";
        }
        if (guidSteam.equals(usuario.getGuidSteam())) {
            return "ok";
        }
        if (usuarioRepository.existsByGuidSteam(guidSteam)) {
            return "ocupado";
        }

        usuario.setGuidSteam(guidSteam);
        usuarioRepository.save(usuario);
        return "ok";
    }

    private boolean verificarFirma(Map<String, String> params) {
        String signedRaw = params.get("openid.signed");
        String sig = params.get("openid.sig");
        if (signedRaw == null || sig == null) {
            return false;
        }

        Map<String, String> form = new LinkedHashMap<>();
        for (String campo : signedRaw.split(",")) {
            String valor = params.get("openid." + campo);
            if (valor == null) {
                return false;
            }
            form.put("openid." + campo, valor);
        }
        form.put("openid.mode", "check_authentication");
        form.put("openid.sig", sig);
        form.put("openid.signed", signedRaw);

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(STEAM_OPENID_ENDPOINT))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(encodeForm(form)))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body() != null && response.body().contains("is_valid:true");
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String encodeForm(Map<String, String> form) {
        return form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
