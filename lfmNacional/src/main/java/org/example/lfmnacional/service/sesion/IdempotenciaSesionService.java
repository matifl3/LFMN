package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.EventoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Calcula claves de idempotencia a partir del contenido del JSON de sesion.
 *
 * Reemplaza la deduplicacion por nombre de archivo del SesionFolderWatcher, que
 * era fragil: el mismo JSON subido con otro nombre se procesaba dos veces.
 */
@Service
public class IdempotenciaSesionService {

    private static final int LONGITUD_CLAVE = 40;

    /**
     * Clave de la sesion completa: misma carrera + mismo contenido = misma clave.
     * No incluye el nombre de archivo ni timestamps externos.
     */
    public String claveSesion(Long carreraId, SesionServerData sesion) {
        StringBuilder sb = new StringBuilder();
        sb.append(carreraId).append('|')
                .append(valor(sesion.trackName())).append('|')
                .append(valor(sesion.trackConfig())).append('|')
                .append(valor(sesion.type())).append('|')
                .append(valor(sesion.durationSecs())).append('|')
                .append(valor(sesion.raceLaps())).append('|')
                .append(sesion.cars() == null ? 0 : sesion.cars().size()).append('|')
                .append(sesion.result() == null ? 0 : sesion.result().size()).append('|')
                .append(sesion.laps() == null ? 0 : sesion.laps().size()).append('|')
                .append(sesion.events() == null ? 0 : sesion.events().size());
        return sha256(sb.toString());
    }

    /**
     * Clave de un incidente autogenerado. Reimportar la misma sesion no debe
     * duplicar incidentes: se identifica carrera + evento concreto.
     */
    public String claveIncidente(Long carreraId, EventoSesionData evento, String guid) {
        StringBuilder sb = new StringBuilder();
        sb.append(carreraId).append('|')
                .append(valor(guid)).append('|')
                .append(valor(evento.type())).append('|')
                .append(valor(evento.carId())).append('|')
                .append(valor(evento.otherCarId())).append('|')
                .append(impacto(evento)).append('|')
                .append(posicion(evento.worldPosition()));
        return sha256(sb.toString());
    }

    /**
     * Clave derivada del nombre de archivo. Solo para la ingesta por carpeta,
     * donde no se tiene el JSON parsed al momento de registrar. La ingesta por
     * HTTP usa {@link #claveSesion}, que es mas fuerte.
     */
    public String claveDesdeNombre(String nombreArchivo) {
        return sha256("archivo|" + valor(nombreArchivo));
    }

    private String impacto(EventoSesionData evento) {
        Double velocidad = evento.impactSpeed();
        // Redondeo a entero: 40.4 y 40.0 (mismo choque, con ruido de float)
        // colapsan a la misma clave. Choques claramente distintos (40 vs 41)
        // siguen siendo incidentes separados.
        return velocidad == null ? "null" : String.valueOf(Math.round(velocidad));
    }

    private String posicion(org.example.lfmnacional.dto.sesion.Posicion3D posicion) {
        if (posicion == null) {
            return "null";
        }
        return valor(posicion.x()) + "," + valor(posicion.y()) + "," + valor(posicion.z());
    }

    private String valor(Object valor) {
        return valor == null ? "null" : valor.toString();
    }

    private String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, LONGITUD_CLAVE);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}