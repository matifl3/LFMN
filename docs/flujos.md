# Flujos de negocio y modelo de tablas — Low Fuel Motorsport

> Documento técnico del backend (Spring Boot) del proyecto **Low Fuel Motorsport**.
> Describe el modelo de 26 tablas y los flujos principales del dominio.

---

## 1. Visión general

El sistema se organiza en torno a 6 flujos de negocio, todos partiendo de un
usuario registrado:

```
┌─────────────────────────────────────────────────────────────────┐
│                        FLUJO CENTRAL                            │
│                                                                 │
│  Usuario ──► Inscripción ──► Carrera ──► Resultados ──► Rating   │
│                (A)            (B)                    + Tabla     │
│                                                      (B)        │
└──────────┬───────────────────┬──────────────────────────────────┘
           │                   │
           ▼                   ▼
     ┌────────────┐     ┌─────────────────┐
     │  Incidentes │     │   Real Penalty   │──► Sanciones (C)
     │   (C)      │────►│   (origen RP)     │────► Apelaciones (D)
     └────────────┘     └─────────────────┘
           │
           ▼
     Sanciones (C) ──► Ajustes Elo / SR

  Flujos auxiliares: Setups (E), Logros/Recompensas (F), Notificaciones.
```

Flujos:
- **A — Inscripción a carrera**: lista de espera (RF-033) y cierre automático 5 min antes (RF-073).
- **B — Carrera → Resultados → Rating**: recálculo Elo/SR al cargar resultados y tabla de campeonato (RF-035).
- **C — Incidentes → Resolución → Sanción**: quorum de 2 votos de comisarios (RF-099).
- **D — Apelación de sanción**: el piloto puede apelar y el admin responde.
- **E — Setups**: publicación, calificación y comentarios.
- **F — Logros y Recompensas**: progreso, obtención y reclamo.

---

## 2. Diagrama de tablas (26 entidades)

### Núcleo

```
usuario
  ├─ id PK
  ├─ email UNIQUE
  ├─ password
  ├─ nombre_piloto
  ├─ foto_perfil
  ├─ guid_steam UNIQUE
  ├─ elo
  ├─ safety_rating
  ├─ rol            (ADMIN / COMISARIO / USUARIO)
  └─ fecha_registro

categoria
  ├─ id PK
  ├─ nombre
  ├─ descripcion
  ├─ elo_minimo / elo_maximo
  ├─ setup_abierto / setup_fijo
  └─ (1:N) carreras, (1:N) campeonatos
```

### Campeonato y carreras

```
categoria 1───N campeonato 1───N campeonato_posicion N───1 usuario
                   │                (puntos, posicion)
                   │                UNIQUE(campeonato_id, usuario_id)
                   │
categoria 1───N carrera 1───N sesion_clasificacion N───1 usuario
                   │   │          (tiempo, diferencia_pole)
                   │   │
                   │   ├──N inscripcion N───1 usuario
                   │   │     (estado, fecha_inscripcion)
                   │   │     UNIQUE(carrera_id, usuario_id)
                   │   │
                   │   └──N resultado_carrera N───1 usuario
                   │         (posicion_final, tiempo_total, vuelta_rapida,
                   │          poles, finalizo, elo_ganado, sr_ganado)
                   │         UNIQUE(carrera_id, usuario_id)
                   │
                   └──N──1 archivo_carrera        (1 archivo → muchas carreras)
                         (nombre, ruta, tipo)      FK archivo_id vive en carrera
```

### Incidentes (comisarios)

```
carrera 1───N incidente N───1 usuario (reportante_id)
              │  ├─N incidente_piloto N───1 usuario   UNIQUE(incidente_id, usuario_id)
              │  │     (rol: PERPETRADOR / VICTIMA)
              │  ├─N voto_comisario N───1 usuario     UNIQUE(incidente_id, comisario_id)
              │  │     (decision, comentario, fecha)
              │  └─1─1 resolucion_incidente 1───1 usuario (comisario)
              │        (explicacion, fecha)           UNIQUE(incidente_id)
              │
              └──(resolución aprobada)──► sancion
```

### Sanciones, rating y apelaciones

```
usuario 1───N sancion N───1 carrera (opcional)
              │      N───1 resolucion_incidente (opcional)
              │      (tipo, valor, motivo, origen, id_externo, fecha)
              │
              ├──> apelacion N───1 usuario
              │       (motivo, estado, respuesta_admin)
              │
usuario 1───N elo_sancion (cambio, motivo, fecha, carrera opcional)
usuario 1───N safety_rating_sancion (cambio, motivo, fecha, carrera opcional)
```

### Setups

```
usuario 1───N setup N───1 categoria
              ├─N setup_calificacion N───1 usuario   UNIQUE(setup_id, usuario_id)
              │     (puntaje)
              └─N setup_comentario N───1 usuario
                    (texto, fecha)
```

### Logros y recompensas

```
logro 1───N recompensa
usuario ──N usuario_logro N──1 logro     UNIQUE(logro_id, usuario_id)
              │      (progreso, obtenido, fecha_obtencion)
              ▼
usuario ──N usuario_recompensa N──1 recompensa  UNIQUE(recompensa_id, usuario_id)
              (reclamada, fecha)
```

### Notificaciones y anuncios

```
notificacion N───1 usuario   (tipo, mensaje, leida, link)
anuncio                        (titulo, contenido, url_imagen) — sin relaciones
```

---

## 3. Flujo A — Inscripción a carrera

Reglas de negocio: cupo máximo (RF-029), lista de espera (RF-033), cierre de
inscripciones 5 min antes (RF-073).

```
                    ┌──────────────────────────────┐
                    │   carrera.estado = PROGRAMADA │
                    └──────────────┬───────────────┘
                                   │ usuario pide inscribirse
                                   ▼
                        ┌───────────────────────┐
                        │  fecha < inicio - 5min │ NO ──► ERROR 400
                        │  (RF-073)             │       "inscripciones cerradas"
                        └───────────┬───────────┘
                                    │ SI
                                    ▼
                        ┌───────────────────────┐
                        │ ¿cupo disponible?      │
                        └──────┬───────────┬────┘
                              SI           NO
                              │            │
                              ▼            ▼
              estado = INSCRIPTO    estado = LISTA_ESPERA (RF-033)
                              │            │
                              ▼            ▼
              [usuario + carrera]    (se ordena por fecha_inscripcion)
              UNIQUE(carrera,usuario)

   Alta de baja del usuario:                        Promoción:
   - usuario con estado INSCRIPTO se da de baja     - 1er LISTA_ESPERA por
   - se libera el cupo                              - fecha pasa a INSCRIPTO
```

Notas:
- `inscripcion.estado`: `INSCRIPTO` / `LISTA_ESPERA` (enum `EstadoInscripcion`).
- `@Scheduled` cada minuto: para carreras a menos de 5 minutos, cierra inscripciones
  (`estado = CERRADA`) y valida quórum de pilotos.
- La baja de un `INSCRIPTO` promueve automáticamente al primero de espera por fecha.

---

## 4. Flujo B — Carrera → Resultados → Rating y Tabla

Al cargar resultados de una carrera (RF-035):

```
        ┌───────────────────────────────┐
        │  Cargar resultados (masivo)    │
        │  CargarResultadosRequest       │
        └──────────────┬────────────────┘
                       ▼
        ┌───────────────────────────────┐
        │  Por cada resultado:           │
        │  - posicion_final, tiempo,     │
        │    vuelta_rapida, poles,       │
        │    finalizo (0/1)              │
        └──────────────┬────────────────┘
                       ▼
        ┌───────────────────────────────┐
        │  EloCalculator                │
        │  - K=32, dispersión=400 (TBD-7│
        │  - por posicion vs promedio   │
        │  elo_ganado = ±delta          │
        └──────────────┬────────────────┘
                       ▼
        ┌───────────────────────────────┐
        │  SrCalculator (Safety Rating) │
        │  +5 finalizo / -5 DNF         │
        │  +10 victoria +8 2º +6 3º     │
        └──────────────┬────────────────┘
                       ▼
        ┌───────────────────────────────┐
        │  Actualizar usuario:          │
        │  usuario.elo += elo_ganado    │
        │  usuario.safety_rating += sr  │
        │  + registrar historial        │
        │  (elo_sancion / sr_sancion    │
        │   con carrera referencia)     │
        └──────────────┬────────────────┘
                       ▼
        ┌───────────────────────────────┐
        │  Tabla de campeonato (RF-035) │
        │  puntos estilo F1 (TBD-1):    │
        │  25-18-15-12-10-8-6-4-2-1     │
        │  → campeonato_posicion        │
        │  (upsert UNIQUE campeonato+   │
        │   usuario, ordenar por puntos │
        │   → posicion)                 │
        └───────────────────────────────┘
```

Notas:
- Vuelta rápida: `countVueltaRapidaByUsuario` (RF-016) para la baja de
  vuelta rápida acumulada por piloto.
- Los valores por defecto de Elo/SR/puntos son interpretación de los TBD
  (TBD-7, TBD-1) hasta que se definan las fórmulas oficiales.
- `resultado_carrera` es UNIQUE(carrera_id, usuario_id): no se puede cargar
  dos veces el mismo piloto en la misma carrera.

---

## 5. Flujo C — Incidentes → Resolución → Sanción

Proceso de comisarios con quorum de 2 votos (RF-099, TBD-6).

```
┌────────────────────────────────────────────────────────────┐
│ 1. REPORTE                                                 │
│    incidente (carrera, reportante, vuelta, descripcion,    │
│               video_url) → estado PENDIENTE                │
│    + incidente_piloto (rol PERPETRADOR / VICTIMA)          │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 2. VOTACIÓN                                                │
│    voto_comisario (comisario, decision, comentario)        │
│    - UNIQUE(incidente_id, comisario_id): 1 voto por comis. │
│    - estados: NADA / ADVERTENCIA / PENALIZADO / DESCALIFIC │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 3. QUÓRUM (TBD-6)                                          │
│    ¿≥2 votos A_FAVOR? ── NO ──► sigue PENDIENTE            │
│                        │                                   │
│                        SI                                  │
│                        ▼                                   │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 4. RESOLUCIÓN                                              │
│    resolucion_incidente (comisario que cierra, explicacion)│
│    incidente.estado = RESUELTO                              │
│    UNIQUE(incidente_id)                                    │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 5. SANCIÓN                                                 │
│    sancion (usuario, carrera, resolucion, tipo, valor,     │
│             motivo, origen=COMISARIO)                      │
│    + ajustes de rating si aplica:                          │
│      elo_sancion (cambio), safety_rating_sancion (cambio)  │
│    + notificación al piloto sancionado                     │
└────────────────────────────────────────────────────────────┘
```

Notas:
- Si la resolución es `DESCALIF`, la sanción aplica también penalidad de
  Safety Rating (RP -20 según TBD-6).
- `sancion.resolucion_id` opcional: permite sanciones sin resolución previa
  (ej. de origen Real Penalty).

---

## 6. Flujo D — Apelación de sanción

```
sancion aplicada al piloto
          │
          ▼
apelacion (sancion, usuario, motivo) → estado PENDIENTE
          │
          ▼
admin responde: estado APROBADA / RECHAZADA (+ respuesta_admin)
          │
          ├─► APROBADA : se revierte el efecto de la sancion
          │              (si aplica, se compensa Elo/SR)
          └─► RECHAZADA: la sancion queda firme
```

Notas:
- `apelacion` es UNIQUE por (sancion_id) — se asume una apelación por sanción.
- Una apelación aprobada revierte los cambios de rating aplicados en el Flujo C.

---

## 7. Flujo E — Setups

```
usuario publica setup (titulo, circuito, vehiculo, archivo,
                       categoria opcional) → fecha_publicacion
          │
          ▼
setup_calificacion (puntaje 1..5)  →  recalcula setup.promedio_calificacion
          │
          ▼
setup_comentario (texto) → se lista con el setup
```

Notas:
- UNIQUE(setup_id, usuario_id) en calificaciones: un usuario califica una vez.
- `categoria.setup_abierto` / `setup_fijo` condicionan la visibilidad de los
  archivos de setup según la categoría.

---

## 8. Flujo F — Logros y Recompensas

```
logro (tipo_condicion, valor_condicion)
  │  condiciones: carreras corridas, victorias, vuelta rapida,
  │               sanciones evitadas, etc. (TipoCondicionLogro)
  ▼
usuario_logro (progreso, obtenido, fecha_obtencion)
  │  - el progreso avanza con los resultados (Flujo B)
  │  - al alcanzar valor_condicion → obtenido = true
  ▼
usuario_recompensa (reclamada) — el usuario reclama la recompensa
```

Notas:
- UNIQUE(logro_id, usuario_id) en `usuario_logro`.
- `recompensa` cuelga de `logro` (una o varias por logro).

---

## 9. Flujo G — Real Penalty (origen de sanciones externas)

Detalle completo en [integracion-real-penalty.md](integracion-real-penalty.md).
Resumen del impacto en el modelo:

```
Real Penalty (UDP) ──► evento de penalidad (driverGUID, tipo, valor)
                          │
                          ▼
              sancion (origen=REAL_PENALTY, id_externo=id del evento)
                          │
                          ├─ correlaciona piloto por guid_steam
                          │
                          └─ idempotente: no duplica si ya existe
                             sancion con mismo origen + id_externo
```

Tipos mapeados: `dt` (drive-through), `sgN` (stop & go), `dsq`
(descalificación) → ajuste de Safety Rating.

---

## 10. Constraints clave del modelo

| Tabla | Constraint |
|---|---|
| `usuario` | `email` UNIQUE, `guid_steam` UNIQUE |
| `inscripcion` | UNIQUE(`carrera_id`, `usuario_id`) |
| `resultado_carrera` | UNIQUE(`carrera_id`, `usuario_id`) |
| `campeonato_posicion` | UNIQUE(`campeonato_id`, `usuario_id`) |
| `incidente_piloto` | UNIQUE(`incidente_id`, `usuario_id`) |
| `voto_comisario` | UNIQUE(`incidente_id`, `comisario_id`) |
| `resolucion_incidente` | `incidente_id` UNIQUE (1:1 con incidente) |
| `setup_calificacion` | UNIQUE(`setup_id`, `usuario_id`) |
| `usuario_logro` | UNIQUE(`logro_id`, `usuario_id`) |
| `usuario_recompensa` | UNIQUE(`recompensa_id`, `usuario_id`) |

FKs opcionales (nullable): `sancion.carrera_id`, `sancion.resolucion_id`,
`elo_sancion.carrera_id`, `safety_rating_sancion.carrera_id`,
`carrera.archivo_id` (una carrera puede no tener archivo aún).
