package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.repository.UsuarioRepository;
import org.example.lfmnacional.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamService {

    public record SteamAuthResult(String resultado, String token) {
    }

    private static final String STEAM_OPENID_ENDPOINT = "https://steamcommunity.com/openid/login";
    private static final String OPENID_NS = "http://specs.openid.net/auth/2.0";
    private static final String OPENID_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    private static final Pattern STEAM_ID_PATTERN =
            Pattern.compile("^https?://steamcommunity\\.com/openid/id/(\\d{17})$");
    private static final String SCOPE_VINCULACION = "steam-vinculacion";
    private static final String SCOPE_AUTH = "steam-auth";
    private static final String SUFFIX_EMAIL = "@steam.local";
    private static final long STATE_TIMEOUT_MINUTOS = 10;

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${steam.realm}")
    private String realm;

    @Value("${steam.return-to}")
    private String returnTo;

    @Value("${steam.return-to-auth}")
    private String returnToAuth;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String generarUrlVinculacion(Long userId) {
        String state = jwtUtil.generarToken(
                String.valueOf(userId),
                Map.of("scope", SCOPE_VINCULACION),
                STATE_TIMEOUT_MINUTOS);
        String callback = returnTo + "?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        return buildOpenIdUrl(callback);
    }

    public String generarUrlAuth() {
        String state = jwtUtil.generarToken(
                UUID.randomUUID().toString(),
                Map.of("scope", SCOPE_AUTH),
                STATE_TIMEOUT_MINUTOS);
        String callback = returnToAuth + "?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        return buildOpenIdUrl(callback);
    }

    public String procesarCallback(Map<String, String> params) {
        String guidSteam = validarYExtraerSteamId(params, returnTo);
        if (guidSteam == null) {
            return "invalido";
        }

        Long userId;
        try {
            userId = jwtUtil.extraerUserId(params.get("state"), SCOPE_VINCULACION);
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

    public SteamAuthResult autenticarOCrear(Map<String, String> params) {
        log.info("Steam callback recibido: mode={} return_to={} identity={} state={}",
                params.get("openid.mode"),
                params.get("openid.return_to"),
                params.get("openid.identity"),
                params.get("state") != null ? "(presente)" : "(null)");

        String guidSteam = validarYExtraerSteamId(params, returnToAuth);
        if (guidSteam == null) {
            log.warn("Steam auth falló: validarYExtraerSteamId retornó null");
            return new SteamAuthResult("invalido", null);
        }
        log.info("Steam auth: guidSteam={}", guidSteam);

        try {
            jwtUtil.validarState(params.get("state"), SCOPE_AUTH);
        } catch (Exception e) {
            log.warn("Steam auth falló: state expirado o inválido", e);
            return new SteamAuthResult("expirado", null);
        }

        Usuario usuario = usuarioRepository.findByGuidSteam(guidSteam).orElse(null);
        if (usuario == null) {
            usuario = crearConSteam(guidSteam);
            log.info("Steam auth: usuario nuevo creado id={}", usuario.getId());
        } else {
            log.info("Steam auth: usuario existente id={}", usuario.getId());
        }
        return new SteamAuthResult("ok", jwtUtil.generarToken(usuario));
    }

    private Usuario crearConSteam(String guidSteam) {
        Usuario usuario = Usuario.builder()
                .email("steam_" + guidSteam + SUFFIX_EMAIL)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .guidSteam(guidSteam)
                .build();
        return usuarioRepository.save(usuario);
    }

    private String buildOpenIdUrl(String returnToUrl) {
        return UriComponentsBuilder.fromUriString(STEAM_OPENID_ENDPOINT)
                .queryParam("openid.ns", OPENID_NS)
                .queryParam("openid.mode", "checkid_setup")
                .queryParam("openid.return_to", returnToUrl)
                .queryParam("openid.realm", realm)
                .queryParam("openid.identity", OPENID_SELECT)
                .queryParam("openid.claimed_id", OPENID_SELECT)
                .build()
                .toUriString();
    }

    private String validarYExtraerSteamId(Map<String, String> params, String expectedReturnTo) {
        String mode = params.get("openid.mode");
        if (!"id_res".equals(mode)) {
            log.warn("Steam validation falló: openid.mode={} (esperado id_res)", mode);
            return null;
        }

        String receivedReturnTo = params.get("openid.return_to");
        if (receivedReturnTo == null || !normalizarUrl(expectedReturnTo).equals(normalizarUrl(receivedReturnTo))) {
            log.warn("Steam validation falló: return_to esperado='{}' recibido='{}'",
                    expectedReturnTo, receivedReturnTo);
            return null;
        }

        if (!verificarFirma(params)) {
            log.warn("Steam validation falló: verificación de firma inválida");
            return null;
        }

        String identidad = params.get("openid.identity");
        Matcher matcher = identidad != null ? STEAM_ID_PATTERN.matcher(identidad) : null;
        if (matcher == null || !matcher.matches()) {
            log.warn("Steam validation falló: openid.identity={} no matchea patrón", identidad);
            return null;
        }
        return matcher.group(1);
    }

    private String normalizarUrl(String url) {
        if (url == null) return "";
        try {
            java.net.URI uri = java.net.URI.create(url);
            return uri.getScheme() + "://" + uri.getAuthority() + uri.getRawPath();
        } catch (Exception e) {
            return url;
        }
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
        form.put("openid.ns", OPENID_NS);
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
            boolean valido = response.body() != null && response.body().contains("is_valid:true");
            if (!valido) {
                log.warn("Steam verificación de firma: respuesta NO válida. body={}", response.body());
            }
            return valido;
        } catch (IOException e) {
            log.error("Steam verificación de firma: error de conexión", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Steam verificación de firma: interrumpido", e);
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
