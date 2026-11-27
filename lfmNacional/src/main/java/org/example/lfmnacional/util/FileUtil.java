package org.example.lfmnacional.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class FileUtil {

    /** Solo un punto seguido de 1-10 alfanumericos: nada de "/", "\" ni "..". */
    private static final Pattern EXTENSION_SEGURA = Pattern.compile("\\.[a-z0-9]{1,10}");

    private FileUtil() {
    }

    /**
     * Extension en minusculas del nombre que mando el cliente, o "" si no tiene o
     * no es una extension simple. El nombre original es dato del cliente: algo como
     * {@code "x./../../evil"} no puede terminar formando parte de la ruta en disco.
     */
    public static String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        int index = nombreOriginal.lastIndexOf('.');
        if (index < 0) return "";
        String extension = nombreOriginal.substring(index).toLowerCase(Locale.ROOT);
        return EXTENSION_SEGURA.matcher(extension).matches() ? extension : "";
    }
}
