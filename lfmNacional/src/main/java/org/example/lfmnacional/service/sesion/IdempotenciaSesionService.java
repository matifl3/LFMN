package org.example.lfmnacional.service.sesion;

import org.example.lfmnacional.dto.sesion.EventoSesionData;
import org.example.lfmnacional.dto.sesion.SesionServerData;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Calcula claves de idempotencia a partir del contenido del JSON de sesion.
 *
 * Reemplaza la deduplicacion por nombre de archivo del SesionFolderWatcher
 * (eliminado): el mismo JSON subido con otro nombre debe colapsar a la misma
 * clave.
 */
@Service
public class IdempotenciaSesionService {

    private static final int LONGITUD_CLAVE = 40;

    /**
     * Serializa la sesion siempre igual: los records salen con sus campos en el
     * orden en que estan declarados, asi que dos JSON con el mismo contenido dan
     * el mismo texto aunque vengan con otro formato, espacios u orden de claves.
     */
    private static final ObjectMapper CANONICO = JsonMapper.builder().build();

    /**
     * Clave de la sesion completa: misma carrera + mismo contenido = misma clave.
     * Se hashea el JSON entero (no el nombre de archivo ni timestamps externos).
     *
     * <p>Antes solo entraban la pista, el tipo y las <em>cantidades</em> de autos,
     * resultados, vueltas y eventos: un JSON corregido (un GUID o un tiempo
     * arreglado) daba la misma clave y se rechazaba como duplicado.
     */
    public String claveSesion(Long carreraId, SesionServerData sesion) {
        return sha256(carreraId + "|" + CANONICO.writeValueAsString(sesion));
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