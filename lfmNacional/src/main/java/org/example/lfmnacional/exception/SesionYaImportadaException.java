package org.example.lfmnacional.exception;

/**
 * La carrera ya tiene una sesion de ese tipo importada con otro contenido. No
 * es un error del JSON: el controller la traduce a 409 con un estado propio
 * para que el frontend ofrezca reimportar en vez de mostrar solo el mensaje.
 */
public class SesionYaImportadaException extends BusinessException {

    private final String tipo;

    public SesionYaImportadaException(String tipo, String message) {
        super(message);
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
}
