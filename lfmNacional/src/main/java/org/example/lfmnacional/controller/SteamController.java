package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.SteamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/steam")
@RequiredArgsConstructor
public class SteamController {

    private final SteamService steamService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/vincular-url")
    public Map<String, String> obtenerUrlVinculacion(@AuthenticationPrincipal Usuario usuario) {
        return Map.of("url", steamService.generarUrlVinculacion(usuario.getId()));
    }

    @GetMapping("/vinculacion/callback")
    public RedirectView procesarCallback(@RequestParam Map<String, String> params) {
        String resultado = steamService.procesarCallback(params);
        return new RedirectView(frontendUrl + "/09-my-profile.html?steam=" + resultado);
    }

    @GetMapping("/auth-url")
    public Map<String, String> obtenerUrlAuth() {
        return Map.of("url", steamService.generarUrlAuth());
    }

    @GetMapping("/auth/callback")
    public RedirectView procesarAuthCallback(@RequestParam Map<String, String> params) {
        SteamService.SteamAuthResult resultado = steamService.autenticarOCrear(params);
        String url;
        if (resultado.usuarioId() != null) {
            String codigo = steamService.generarCodigoAuth(resultado.usuarioId());
            url = frontendUrl + "/02-auth.html?steam=ok&codigo=" + codigo;
        } else if ("nuevo".equals(resultado.resultado())) {
            url = frontendUrl + "/02-auth.html?steam=nuevo&guid=" + resultado.guidSteam();
        } else {
            url = frontendUrl + "/02-auth.html?steam=" + resultado.resultado();
        }
        return new RedirectView(url);
    }

    @PostMapping("/completar")
    public ResponseEntity<Map<String, String>> completarAuth(@RequestBody Map<String, String> body) {
        String token = steamService.completarCodigo(body.get("codigo"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "INVALID_CODE", "mensaje", "El código expiró o ya fue usado"));
        }
        return ResponseEntity.ok(Map.of("token", token));
    }
}
