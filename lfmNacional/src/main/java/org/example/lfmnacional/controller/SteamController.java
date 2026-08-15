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
}
