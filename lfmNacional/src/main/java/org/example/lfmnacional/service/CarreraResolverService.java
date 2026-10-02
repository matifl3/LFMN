package org.example.lfmnacional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarreraResolverService {

    boolean guidValido(String guid) {
        return guid != null && !guid.isBlank();
    }
}