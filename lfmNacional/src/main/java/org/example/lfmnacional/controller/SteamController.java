package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.entity.Usuario;
import org.example.lfmnacional.service.SteamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
        if (resultado.token() != null) {
            url = frontendUrl + "/02-auth.html?steam=ok&token=" + resultado.token();
        } else if ("nuevo".equals(resultado.resultado())) {
            url = frontendUrl + "/02-auth.html?steam=nuevo&guid=" + resultado.guidSteam();
        } else {
            url = frontendUrl + "/02-auth.html?steam=" + resultado.resultado();
        }
        return new RedirectView(url);
    }
}
