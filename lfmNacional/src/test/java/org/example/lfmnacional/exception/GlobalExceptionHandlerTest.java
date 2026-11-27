package org.example.lfmnacional.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/setups/1");

    @Test
    void permisoDenegadoEs403ConSuMensaje() {
        var r = handler.handleAccessDenied(new PermisoDenegadoException("No tenes permiso para editar este setup"), request);

        assertThat(r.getStatusCode().value()).isEqualTo(403);
        assertThat(r.getBody().message()).isEqualTo("No tenes permiso para editar este setup");
    }

    @Test
    void elDePreAuthorizeNoMuestraElAccessDeniedEnIngles() {
        var r = handler.handleAccessDenied(new AccessDeniedException("Access Denied"), request);

        assertThat(r.getStatusCode().value()).isEqualTo(403);
        assertThat(r.getBody().message()).isEqualTo("No tienes permisos para realizar esta accion");
    }
}
