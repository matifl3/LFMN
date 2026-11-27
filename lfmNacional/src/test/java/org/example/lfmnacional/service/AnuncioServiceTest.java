package org.example.lfmnacional.service;

import org.example.lfmnacional.dto.anuncio.AnuncioRequest;
import org.example.lfmnacional.entity.Anuncio;
import org.example.lfmnacional.exception.ResourceNotFoundException;
import org.example.lfmnacional.repository.AnuncioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceTest {

    @Mock
    private AnuncioRepository anuncioRepository;

    @InjectMocks
    private AnuncioService anuncioService;

    @Test
    void updateReemplazaTituloContenidoEImagen() {
        Anuncio existente = Anuncio.builder().id(5L).titulo("Viejo").contenido("viejo")
                .urlImagen("/api/imagenes/a.png").destacado(true).build();
        when(anuncioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));

        var r = anuncioService.update(5L, new AnuncioRequest("Nuevo", "texto nuevo", null));

        assertThat(r.titulo()).isEqualTo("Nuevo");
        assertThat(r.contenido()).isEqualTo("texto nuevo");
        assertThat(r.urlImagen()).isNull();
        assertThat(r.destacado()).isTrue();
    }

    @Test
    void updateDeAnuncioInexistenteEs404() {
        when(anuncioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> anuncioService.update(99L, new AnuncioRequest("t", "c", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
