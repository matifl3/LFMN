package org.example.lfmnacional.security;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueo de fuerza bruta por CUENTA. El {@link RateLimitFilter} limita por IP, pero
 * la IP sale de X-Forwarded-For y se puede rotar; este contador va por email, asi
 * que probar passwords contra una cuenta se frena igual venga de donde venga.
 * Solo cuentan los intentos fallidos y un login correcto resetea el contador.
 * En memoria: alcanza para una sola instancia (Render).
 */
@Component
public class LoginIntentos {

    static final int MAX_FALLOS = 5;
    static final long VENTANA_MILIS = 15 * 60_000L;
    static final long BLOQUEO_MILIS = 15 * 60_000L;
    private static final int MAX_ENTRADAS = 10_000;

    private record Estado(int fallos, long primerFallo, long bloqueadoHasta) {
    }

    private final Map<String, Estado> porCuenta = new ConcurrentHashMap<>();

    public boolean bloqueado(String email) {
        Estado estado = porCuenta.get(clave(email));
        return estado != null && estado.bloqueadoHasta() > ahora();
    }

    public void registrarFallo(String email) {
        long ahora = ahora();
        if (porCuenta.size() > MAX_ENTRADAS) {
            limpiar(ahora);
        }
        porCuenta.compute(clave(email), (k, previo) -> {
            if (previo == null || ahora - previo.primerFallo() > VENTANA_MILIS) {
                return new Estado(1, ahora, 0);
            }
            int fallos = previo.fallos() + 1;
            long bloqueadoHasta = fallos >= MAX_FALLOS ? ahora + BLOQUEO_MILIS : previo.bloqueadoHasta();
            return new Estado(fallos, previo.primerFallo(), bloqueadoHasta);
        });
    }

    public void registrarExito(String email) {
        porCuenta.remove(clave(email));
    }

    private void limpiar(long ahora) {
        porCuenta.entrySet().removeIf(e -> e.getValue().bloqueadoHasta() < ahora
                && ahora - e.getValue().primerFallo() > VENTANA_MILIS);
    }

    private static String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    long ahora() {
        return System.currentTimeMillis();
    }
}
