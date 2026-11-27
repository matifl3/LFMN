package org.example.lfmnacional.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilTest {

    @Test
    void extensionSimpleEnMinusculas() {
        assertThat(FileUtil.obtenerExtension("Setup Final.ZIP")).isEqualTo(".zip");
        assertThat(FileUtil.obtenerExtension("vuelta.ini")).isEqualTo(".ini");
    }

    @Test
    void sinExtensionDevuelveVacio() {
        assertThat(FileUtil.obtenerExtension("README")).isEmpty();
        assertThat(FileUtil.obtenerExtension(null)).isEmpty();
    }

    @Test
    void unNombreConRutaNoPuedeColarseEnElDisco() {
        assertThat(FileUtil.obtenerExtension("x./../../etc/cron.d/evil")).isEmpty();
        assertThat(FileUtil.obtenerExtension("a.png/../../x")).isEmpty();
        assertThat(FileUtil.obtenerExtension("a.\\..\\x")).isEmpty();
        assertThat(FileUtil.obtenerExtension("a.p ng")).isEmpty();
    }
}
