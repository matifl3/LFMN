package org.example.lfmnacional.controller;

import lombok.RequiredArgsConstructor;
import org.example.lfmnacional.service.puntos.SistemaPuntosCatalogo;
import org.example.lfmnacional.service.puntos.SistemaPuntosFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sistemas-puntos")
@RequiredArgsConstructor
public class SistemaPuntosController {

    private final SistemaPuntosFactory sistemaPuntosFactory;

    /**
     * Catalogo de esquemas de puntaje disponibles. Publico a proposito: es
     * metadato de configuracion de la liga, no dato de usuario, y lo consume el
     * selector tanto en el panel de admin como en el de organizador.
     */
    @GetMapping
    public List<SistemaPuntosCatalogo> listar() {
        return sistemaPuntosFactory.disponibles();
    }
}