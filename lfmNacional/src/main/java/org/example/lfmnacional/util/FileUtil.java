package org.example.lfmnacional.util;

public final class FileUtil {

    private FileUtil() {
    }

    public static String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null) return "";
        int index = nombreOriginal.lastIndexOf('.');
        return index >= 0 ? nombreOriginal.substring(index) : "";
    }
}
