# Integración Real Penalty — Assetto Corsa + Backend

> Documento técnico de diseño para la integración entre el servidor de
> Assetto Corsa (con el plugin **Real Penalty**) y el backend del proyecto
> **Low Fuel Motorsport** (Spring Boot).

---

## 1. Visión general

Real Penalty (**RP**, de Davide Bolognesi) es un plugin de pago para el
servidor dedicado de Assetto Corsa. Corre como *wrapper* de Python junto al
binario `acServer` y agrega un sistema avanzado de penalidades, control de
pista y driver swaps (endurance).

El flujo completo de la integración:

```
┌────────────────────────────┐
│       acServer (AC)        │
│  servidor dedicado del juego│
└──────────▲───────────────┬──┘
           │ broadcast UDP  │ comandos de penalidad
           │ (eventos, cars)│ (dt / sgN / dsq / segundos)
┌──────────┴───────────────▼──┐
│       Real Penalty          │
│   plugin (wrapper Python)   │
│  - detecta infracciones     │
│  - aplica sanción al server │
└──────────┬─────────────────┘
           │ feed de eventos JSON (UDP)
           │ handshake + password
┌──────────▼─────────────────┐
│   Backend Low Fuel (Spring) │
│   UdpPenaltyListener        │
│   → parsea, persiste,       │
│     notifica (RF-062)       │
└─────────────────────────────┘
```

- **RP aplica la penalidad real en el servidor del juego** (drive-through,
  stop & go, descalificación, o segundos al final de la carrera).
- **RP emite además un feed de eventos JSON por UDP** con la información de
  esas penalidades y otros eventos. Ese es el "paquete JSON" que consume
  nuestro programa.
- Nuestro backend **no** le envía penalidades al servidor: solo las recibe y
  las registra (historial, notificaciones, estadísticas).

---

## 2. Implementación de Real Penalty en el servidor

### 2.1 Instalación

1. Obtener el plugin "SERVER PLUGIN" de RP (Patreon oficial del autor).
2. Descomprimir el zip en la carpeta del servidor dedicado de Assetto Corsa.
3. Requiere la licencia `ACSM.License` para la interfaz de configuración.
4. Activar el plugin junto al arranque de `acServer` (el wrapper se inicia
   como proceso acompañante; en despliegues con Docker se supervisa con
   `tini`).

### 2.2 Configuración de puertos en `server_cfg.ini`

El servidor dedicado de AC expone una interfaz UDP para plugins. Se
configura en `server_cfg.ini`:

| Clave                | Valor de ejemplo | Descripción |
|----------------------|------------------|-------------|
| `UDP_PLUGIN_ADDRESS` | `127.0.0.1:50041` | IP y puerto al que `acServer` envía el broadcast (donde escucha RP). |
| `UDP_PLUGIN_PORT`    | `50041`          | Puerto de escucha del plugin (mismo que el address). |
| `UDP_PLUGIN_LOCAL_PORT` | `50042`       | Puerto local en el que `acServer` escucha las respuestas del plugin. |
| `HTTP_PORT`          | `8081`           | Puerto HTTP del servidor. |

Real Penalty agrega sus propios puertos (tabla en §5):

| Clave de RP        | Valor por defecto | Descripción |
|--------------------|-------------------|-------------|
| `UDP_PORT`         | igual a `UDP_PLUGIN_ADDRESS` | Puerto UDP donde RP recibe el broadcast de AC. |
| `UDP_RESPONSE`     | `IP:UDP_PLUGIN_LOCAL_PORT`  | IP y puerto de respuesta hacia `acServer`. |
| `APP_TCP_PORT`     | `53000` (o `HTTP_PORT + 27`) | Puerto TCP donde escucha la app cliente. |
| `APP_UDP_PORT`     | `53000`           | Puerto UDP donde escucha la app cliente. |
| `OTHER_UDP_PLUGIN` | —                 | Relay hacia otros plugins UDP (IP:Puerto; separados por `;`). |
| `UDP_PORT` (relay) | —                 | Puertos de escucha de los otros plugins. |

> Nota: los puertos de la app cliente deben abrirse en firewall/router.
> El rango permitido para `APP_TCP_PORT` es 53000–53020.

### 2.3 Reglas de penalidad (`penalty_settings.ini`)

RP detecta infracciones usando el broadcast UDP de AC (posición, velocidad,
cortes, sectores, eventos de colisión) y las penaliza según estas reglas:

| Sección       | Tipo de penalidad configurable | Descripción |
|---------------|--------------------------------|-------------|
| `[General]`   | `dt`, `sgN`, `dsq`, `n`        | Activar/desactivar tipos, vueltas para cumplir la sanción, segundos finales por penalidad no cumplida. |
| `[Cutting]`   | `dt`, `sgN`, `n`               | Cortes de pista (límites de trazada). Umbral de avisos, velocidad mínima, tiempo entre cortes. |
| `[Speeding]`  | `dt`, `sg10`, `dsq`            | Exceso de velocidad en carril de pits (tramos según exceso). |
| `[Crossing]`  | `dt`                           | Cruzar la línea de entrada/salida de pits. |
| `[Jump_Start]`| `dt`, `sg10`, `dsq`            | Largada anticipada (tramos según severidad). |
| `[Drs]`       | `dt`, `sgN`, `n`               | Uso ilegal de DRS (distancia al auto de adelante, usos máximos). |
| `[Blue_Flag]` | umbral de tiempo               | No ceder paso con bandera azul. |

Significado de los tipos de penalidad:

| Tipo     | Descripción |
|----------|-------------|
| `dt`     | **Drive-through**: pasar por pits sin detenerse (velocidad legal). |
| `sgN`    | **Stop & Go N segundos**: detenerse en pits el tiempo indicado. |
| `dsq`    | **Descalificación**: expulsión del piloto de la sesión (kick). |
| `n`      | **N segundos** sumados al tiempo final de la carrera (si no se cumple la sanción en pista). |

### 2.4 Cómo RP aplica la sanción al servidor

1. RP recibe el broadcast UDP de `acServer` y monitoriza el comportamiento
   de cada piloto (posición, velocidad, cortes, salidas de pista, DRS).
2. Al detectar una infracción, decide la sanción según `penalty_settings.ini`.
3. Envía el comando de penalidad de vuelta a `acServer` por el canal de
   respuesta (`UDP_RESPONSE`), por ejemplo drive-through, stop & go o
   descalificación (kick).
4. La app **Real Penalty Client** (obligatoria, instalada en AC del piloto)
   se conecta al servidor por TCP/UDP y muestra el estado de la sanción
   (cuenta regresiva, "GO" para salir de pits, etc.). Si el piloto no la
   cumple en las vueltas permitidas, RP convierte la sanción en tiempo al
   final o lo descalifica.
5. Durante el mismo flujo, RP publica el evento JSON en su feed UDP (§3).

---

## 3. Contrato JSON: paquete que recibe el programa

### 3.1 Transporte y handshake

Real Penalty expone un feed de eventos UDP. Para empezar a recibir, el
cliente (nuestro backend) envía un mensaje de inicio:

```json
{ "request": "start", "password": "secret-de-rp" }
```

y a partir de ahí RP transmite los eventos por UDP como datagramas JSON.

> **IMPORTANTE — Esquema propietario:** el formato exacto de campo por campo
> que emite Real Penalty es propiedad del plugin y debe validarse contra la
> versión instalada. Este documento define el **esquema normalizado propuesto**
> que el backend debe aceptar, y una **capa adaptadora** que normaliza el
> evento real de RP a nuestra entidad. El ejemplo es de referencia.

### 3.2 Esquema normalizado propuesto

```json
{
  "evento": "penalidad_aplicada",
  "eventoId": "a1b2c3d4-5678-4abc-9def-0123456789ab",
  "timestamp": "2026-08-04T20:15:00Z",
  "raceId": "carrera-2026-r3",
  "sesion": "RACE",
  "carId": 17,
  "driverName": "Matias",
  "driverGUID": "76561198012345678",
  "tipo": "dt",
  "segundos": 0,
  "vuelta": 12,
  "sector": 2,
  "motivo": "corte_de_pista",
  "severidad": "warning"
}
```

### 3.3 Descripción de campos

| Campo          | Tipo     | Descripción |
|----------------|----------|-------------|
| `evento`       | string   | Tipo de evento (`penalidad_aplicada`, `penalidad_cumplida`, `descalificacion`, `chat`, `sesion_iniciada`, …). |
| `eventoId`     | string   | Identificador único del evento (clave de idempotencia). |
| `timestamp`    | string   | Fecha/hora en formato ISO-8601 (UTC). |
| `raceId`       | string   | Identificador de la carrera en nuestra plataforma (opcional si RP no lo conoce; se correlaciona por sesión). |
| `sesion`       | string   | Tipo de sesión: `PRACTICE`, `QUALIFY`, `RACE`. |
| `carId`        | int      | ID del auto/piloto en la sesión del servidor. |
| `driverName`   | string   | Nombre del piloto. |
| `driverGUID`   | string   | Steam GUID del piloto (clave de correlación con el usuario). |
| `tipo`         | string   | `dt`, `sg`, `dsq`, o `seconds`. |
| `segundos`     | int      | Segundos de la sanción (`0` para drive-through). |
| `vuelta`       | int      | Vuelta en que ocurrió el evento. |
| `sector`       | int      | Sector en que ocurrió (1-3). |
| `motivo`       | string   | Infracción: `corte_de_pista`, `velocidad_pits`, `cruce_linea`, `largada`, `drs`, `bandera_azul`, `otro`. |
| `severidad`    | string   | `warning`, `penalidad`, `descalificacion`. |

### 3.4 Mapeo a la entidad del dominio

La capa adaptadora traduce el JSON normalizado a la entidad `Penalizacion`:

| Campo JSON          | Entidad `Penalizacion`     |
|---------------------|----------------------------|
| `eventoId`          | `idExterno` (único)        |
| `timestamp`         | `fecha`                    |
| `driverGUID`        | `piloto.guidSteam`         |
| `tipo` + `segundos` | `tipo` (`DT`, `SG`, `DSQ`, `SEGUNDOS`) y `segundos` |
| `motivo`            | `motivo`                   |
| `vuelta` / `sector` | `vuelta` / `sector`        |
| `sesion`            | `sesion`                   |

> La correlación `driverGUID → usuario` y `sesion → carrera` debe resolverse
> contra la base antes de persistir; si no se resuelve, el evento queda en
> cola de reintentos (§4.4).

---

## 4. Recepción en el backend Spring Boot

### 4.1 Componente de escucha UDP

Clase `UdpPenaltyListener` (componente Spring):

- Abre un `DatagramSocket` (o `ReactorNetty`/`Netty`) en el puerto de escucha
  configurado (`rp.udp.listen-port`).
- Al iniciar, realiza el handshake enviando el mensaje `start` con la
  password (`rp.udp.password`).
- Lee datagramas, los decodifica como texto UTF-8 y los entrega al
  deserializador.

### 4.2 Deserialización y validación

- Parseo con **Jackson** a un DTO (`PenaltyEventDto`) cuyos nombres de campo
  usan `@JsonProperty` para tolerar variantes del esquema real de RP.
- Validación con Bean Validation: campos obligatorios (`eventoId`,
  `driverGUID`, `tipo`); se descartan eventos inválidos con log.

### 4.3 Idempotencia

- Antes de persistir se verifica la existencia de `idExterno = eventoId`.
- Si el evento ya existe, se ignora (los datagramas UDP pueden duplicarse).

### 4.4 Procesamiento y efectos

Al persistir la penalidad, el sistema dispara:

| Efecto                     | Requisito vinculado |
|----------------------------|---------------------|
| Guardar penalidad del piloto | RF-020 (ver penalizaciones) |
| Notificar al piloto         | RF-062 (notificar penalización) |
| Ajustar Safety Rating        | RF-041 / RF-095 (según resolución) |
| Registrar historial de SR    | RF-019 |
| Registrar historial de Elo   | RF-018 (si la sanción impacta Elo) |

Si el `driverGUID` no está asociado a ningún usuario, el evento se encola y
se reintenta (backoff) hasta resolver la correlación o vencer el límite.

### 4.5 Manejo de errores

- **JSON malformado**: se loguea el datagrama sin crashear el listener.
- **Timeout de handshake**: reintento periódico del mensaje `start`.
- **Evento no correlacionado**: cola de reintentos con backoff exponencial.
- Toda excepción se registra con `@Slf4j`; el listener permanece activo.

---

## 5. Configuración y despliegue

### 5.1 Tabla de puertos

| Origen → Destino      | Puerto | Protocolo | Uso |
|-----------------------|--------|-----------|-----|
| acServer → RP         | `50041` (UDP_PLUGIN_ADDRESS) | UDP | Broadcast de eventos de AC. |
| RP → acServer         | `50042` (UDP_PLUGIN_LOCAL_PORT) | UDP | Comandos de penalidad. |
| Clientes (app RP) → RP | `53000` (y 53001-53020) | TCP/UDP | Conexión de la app del piloto. |
| RP → Backend          | `50050` (rp.udp.listen-port) | UDP | Feed de eventos JSON. |

### 5.2 Variables de entorno del backend

```
RP_UDP_LISTEN_PORT=50050
RP_UDP_PASSWORD=secret-de-rp
```

### 5.3 Notas de firewall / red

- Los puertos de la app cliente (53000+) deben estar abiertos y redirigidos
  si el servidor está detrás de NAT.
- El puerto del feed hacia el backend debe estar abierto desde el host del
  servidor de AC hacia el host del backend.
- Los números de puerto que `acServer` anuncia a los clientes deben coincidir
  con los externos (túneles/port forwarding).

---

## 6. Trazabilidad con los requisitos

| Requisito | Descripción | Relación con esta integración |
|-----------|-------------|------------------------------|
| RF-034 / RF-035 | Admin penaliza con puestos / segundos | Las sanciones post-carrera del admin complementan las automáticas de RP. |
| RF-048 | Comisario asigna penalización | Comisario resuelve incidentes reportados (flujo manual, independiente de RP). |
| RF-051 | Comisario agrega comentario | Comentario asociado a la resolución. |
| RF-062 | Notificar penalización | Disparada al recibir el evento JSON de RP. |
| RF-018 / RF-019 | Historial de Elo / Safety Rating | Impacto de la sanción registrado en historial. |
| RF-020 | Ver penalizaciones | Lista alimentada por los eventos de RP persistidos. |

---

## 7. Pendientes de validación

- [ ] Confirmar el esquema JSON **real** emitido por la versión de Real
      Penalty instalada (capturar un evento con `penalidad_aplicada`).
- [ ] Definir si el feed requiere el mensaje `start` cada reconexión o hay
      timeout de sesión.
- [ ] Definir la fórmula de impacto de penalidades sobre Elo/SR (TBD-1 del
      documento de requisitos).
- [ ] Acordar cómo se correlaciona la sesión del servidor con la carrera de
      la plataforma (`raceId`).
