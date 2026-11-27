package org.example.lfmnacional.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * 403 por una regla de permiso sobre un recurso puntual ("este setup no es tuyo").
 * A diferencia del {@link AccessDeniedException} generico de {@code @PreAuthorize}
 * (que dice "Access Denied"), su mensaje esta pensado para mostrarse en el front.
 */
public class PermisoDenegadoException extends AccessDeniedException {

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
