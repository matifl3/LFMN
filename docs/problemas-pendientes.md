# Problemas pendientes — revisión de código 24/11/2026

Hallazgos de una revisión leyendo el código del backend y frontend. **Todos corregidos el
26/11/2026**, cada uno con su test; el detalle de cada solución está en su sección.

Rutas relativas a `lfmNacional/src/main/java/org/example/lfmnacional/` salvo que se indique.

| # | Problema | Severidad | Impacto |
|---|---|---|---|
| 1 | ✅ Los autos doblados pueden quedar adelante al importar la carrera | **Alta** | Clasificación, Elo, SR y puntos del campeonato |
| 2 | ✅ La sanción de segundos reclasifica solo por tiempo | **Alta** | Clasificación y tabla del campeonato |
| 3 | ✅ La idempotencia de sesiones no es por contenido | Media | No se puede reimportar un JSON corregido |
| 4 | ✅ Cantidad de participantes del Elo incluye resultados sin posición | Baja | Elo levemente desviado |
| 5 | ✅ `recalcularPuntos` ignora el estado del campeonato | Baja | Puntos que aparecen de la nada |
| 6 | ✅ El Elo por sanción puede quedar negativo | Baja | Inconsistencia de datos |
| 7 | ✅ El check de la tabla de posiciones está en el controller | Deuda | Va contra la regla 3 de `arquitectura-y-patrones.md` |
| 8 | ✅ 56 `catchError(() => of(...))` en el frontend | Deuda | Errores del backend invisibles |

---

## 1. Autos doblados adelante al importar la carrera — Alta ✅ Resuelto

> **Resuelto (26/11/2026):** los que terminaron se ordenan por vueltas completadas (desc) y después
> por `totalTime`. Las vueltas se guardan en `ResultadoCarrera.vueltasCompletadas`. Lo cubre
> `ClasificacionDobladosIntegracionTest` con `testJsonServidor/2026_8_8_RACE_doblado_y_dnf.json`
> (copia en `src/test/resources/sesiones/`).

**Dónde:** `service/ResultadoImportService.java:48`

```java
List<ResultadoSesionData> finalizaron = conGuid.stream()
        .filter(r -> r.totalTime() != null && r.totalTime() > SIN_TIEMPO)
        .sorted(java.util.Comparator.comparing(ResultadoSesionData::totalTime))
        .toList();
```

**Problema:** los que terminaron se ordenan solo por `totalTime`. En Assetto Corsa, el
`TotalTime` de un piloto doblado es el tiempo que tardó en completar **sus** vueltas, que son
menos. Ese tiempo puede ser menor que el del ganador.

**Escenario:** carrera a 20 vueltas. El ganador hace 20 vueltas en 40:00. Un piloto a una vuelta
completa 19 vueltas en 39:10 y cruza la meta detrás del líder. Con el orden actual, el doblado
queda **1°** y el ganador **2°**. Eso se propaga a `eloGanado`, `srGanado`, logros y
`campeonato_posicion`.

**Por qué no se detectó:** `testJsonServidor/2026_8_7_16_31_RACE.json` tiene un solo piloto con
tiempo (los otros dos tienen `TotalTime = 0`).

**Solución propuesta:** usar el orden del array `Result` del JSON, que AC ya entrega en el orden
real de llegada. Si se prefiere no depender de eso: ordenar por vueltas completadas descendente
y, a igualdad, por `totalTime` ascendente (`vueltasCompletadas()` ya existe en la misma clase).

**Test a agregar:** un JSON de RACE con al menos 3 pilotos, uno doblado con `TotalTime` menor que
el del ganador, y verificar `posicionFinal`.

---

## 2. La sanción de segundos reclasifica solo por tiempo — Alta ✅ Resuelto

> **Resuelto (26/11/2026):** `SancionService.reclasificar` rehace la clasificación desde
> `ResultadoCarrera.posicionOriginal` (la de la carga) cada vez que se aplica o revierte una sanción
> de PUESTOS o SEGUNDOS: si hay alguna de segundos activa reordena por terminó → vueltas → tiempo
> (los DNF quedan al final en su orden), y después aplica las de puestos activas por fecha.
> Tests en `ClasificacionDobladosIntegracionTest`.
>
> **Filas viejas:** las cargadas antes del cambio tienen `posicion_original = NULL` y se completan
> con la posición actual la primera vez que se reclasifica la carrera. Si esa carrera ya tenía una
> sanción de PUESTOS aplicada, esa pérdida quedaría contada dos veces.

**Dónde:** `service/SancionService.java:258` (`ajustarTiempo`) y `:277` (`reclasificarPorTiempo`)

```java
List<ResultadoCarrera> porTiempo = resultadosDeCarrera(carreraId).stream()
        .filter(r -> r.getTiempoTotal() != null)
        .sorted(Comparator.comparing(ResultadoCarrera::getTiempoTotal))
        ...
renumerar(porTiempo);
```

Tiene tres problemas:

1. **El mismo de los doblados (#1).** Al aplicar cualquier sanción de segundos, toda la carrera
   se reordena por tiempo total y un doblado puede saltar al 1° puesto, aunque la importación
   haya estado bien.
2. **Choque de posiciones con los DNF.** Los resultados sin `tiempoTotal` quedan fuera del
   renumerado y mantienen su `posicionFinal` vieja. Si los que tienen tiempo se renumeran 1..N,
   puede haber dos pilotos con la misma posición.
   - Ojo: el import guarda `dato.totalTime()` también para los DNF (puede ser `0`, no `null`).
     Un DNF con `tiempoTotal = 0` entra al orden y queda **1°**. Hay que confirmarlo contra datos
     reales.
3. **Pisa sanciones de puestos anteriores.** Si un piloto tiene una sanción `PUESTOS` aplicada y
   después alguien recibe una sanción `SEGUNDOS`, el reordenamiento por tiempo deshace la pérdida
   de puestos sin revertirla formalmente (la sanción sigue con `efectosAplicados = true`). Si
   después se revierte la de puestos, mueve al piloto hacia **arriba** desde una posición que ya
   no era la penalizada.

**Solución propuesta:**

- Reclasificar con el mismo criterio que la importación: primero `finalizo`, después vueltas
  completadas y por último `tiempoTotal`. Para eso `ResultadoCarrera` necesita guardar las
  vueltas completadas (hoy no tiene ese campo; se puede contar desde `VueltaCarrera`).
- Excluir de la reclasificación a los DNF (`finalizo = false`) y dejarlos al final, renumerados
  detrás de los que terminaron.
- Para las interacciones entre sanciones, conviene recalcular la clasificación desde cero:
  partir del orden base de la carrera y aplicar todas las sanciones activas en orden de fecha.
  Así revertir una no depende del estado que dejó la otra.

**Tests a agregar:**
- Sanción de segundos en una carrera con un doblado y un DNF → nadie sube indebidamente y no hay
  posiciones repetidas.
- Sanción de puestos + sanción de segundos a otro piloto → la pérdida de puestos se mantiene.
- Revertir la de puestos después de la de segundos → vuelve al orden correcto.

Ya existe `SancionEfectosIntegracionTest` como base.

---

## 3. La idempotencia de sesiones no es "por contenido" — Media ✅ Resuelto

> **Resuelto (26/11/2026), opción "reimportar explícito":** la clave hashea el JSON entero. Un JSON
> distinto para una sesión ya importada devuelve `409` con `estado = OTRA_SESION_IMPORTADA`, y se
> reemplaza con `POST /api/sesiones/reimportar?carreraId=N` (ADMIN/COMISARIO; el panel de admin lo
> ofrece con confirmación). Detalle en §2.8 de `arquitectura-y-patrones.md`. Tests:
> `ReimportacionSesionIntegracionTest`.
>
> Sesiones importadas antes del cambio tienen la clave vieja: reenviar el mismo archivo ya no da
> `YA_PROCESADA` sino `OTRA_SESION_IMPORTADA` (no reprocesa en ninguno de los dos casos).

**Dónde:** `service/sesion/IdempotenciaSesionService.java:28` (`claveSesion`)

**Problema:** la clave hashea `carreraId`, pista, configuración, tipo, duración, vueltas de
carrera y la **cantidad** de autos, resultados, vueltas y eventos. No usa los datos en sí. Dos
JSON de la misma carrera y el mismo tipo con los mismos conteos dan la misma clave.

**Escenario:** se importa el RACE, se detecta un error en el archivo (un GUID mal, un tiempo
corregido) y se reimporta el JSON corregido. Como los conteos no cambian,
`SesionServidorController` devuelve `409` y el cambio no entra. La única salida es tocar la base.

Además, la documentación (`AGENTS.md` y §2.8 de `arquitectura-y-patrones.md`) dice "idempotente por
contenido (SHA-256)", que no es lo que hace.

**Solución propuesta:** decidir qué se quiere.
- **Si "mismo archivo = duplicado":** hashear los bytes del JSON recibido (o el JSON serializado
  canónico). Un JSON corregido daría otra clave, pero entonces hay que definir qué pasa al
  importar un segundo RACE para una carrera que ya tiene resultados (`cargarResultados` hoy lo
  rechaza con `BusinessException`).
- **Si "una sesión por carrera y tipo":** que la clave sea `carreraId + tipo` y agregar un
  endpoint explícito de reimportación que borre lo anterior (resultados, vueltas, puntos,
  Elo/SR revertidos) antes de cargar.

En cualquiera de los dos casos, actualizar la documentación.

**Test a agregar:** en `IdempotenciaSesionServiceTest`, dos sesiones con los mismos conteos y
distinto contenido → la expectativa que se defina.

---

## 4. Participantes del Elo incluye resultados sin posición — Baja ✅ Resuelto

> **Resuelto (26/11/2026):** `recalcularEloYSafetyRating` filtra primero los resultados con posición
> y usa esa lista para el total y los rivales. Test: `ResultadoCarreraServiceEloTest`.

**Dónde:** `service/ResultadoCarreraService.java:151`

```java
int cambioElo = eloCalculator.calcularCambio(eloPropio, posicion, resultados.size(), rivales);
```

**Problema:** los resultados con `posicionFinal == null` se saltean (`continue`), pero cuentan en
`resultados.size()` y su Elo entra en `rivales`. Con la importación actual siempre hay posición,
así que solo afecta a la carga manual por `POST /api/resultados`.

**Solución propuesta:** filtrar primero los resultados con posición y usar esa lista para el
total y para los rivales.

---

## 5. `recalcularPuntos` ignora el estado del campeonato — Baja ✅ Resuelto

> **Resuelto (26/11/2026):** `actualizarPuntos` anota en cada resultado si sumó
> (`ResultadoCarrera.sumaAlCampeonato` = el campeonato estaba `ACTIVO` al cargarlo), y
> `recalcularPuntos` solo suma esos. Se mantiene que recalcular corra con el campeonato cerrado
> (corrige la tabla tras una sanción o apelación). Las filas viejas (`NULL`) siguen sumando, como
> antes. No se unificaron los dos métodos: sigue habiendo lógica de suma duplicada. Test:
> `SancionEfectosIntegracionTest.carreraCargadaConCampeonatoCerradoNoSumaAlRecalcular`.

**Dónde:** `service/CampeonatoService.java:219` vs `:285`

**Problema:** `actualizarPuntos` solo suma si el campeonato está `ACTIVO`. `recalcularPuntos`
(que se llama al aplicar o revertir una sanción de puestos o de segundos) suma **todas** las
carreras del campeonato, sin mirar el estado.

**Escenario:** se cargan resultados mientras el campeonato no está activo → no suman. Más tarde
se aplica una sanción en cualquier carrera de ese campeonato → la tabla se rehace y aparecen los
puntos de esas carreras que nunca se habían contado.

**Solución propuesta:** decidir la regla. Lo más simple es que `recalcularPuntos` sea la fuente de
verdad y `actualizarPuntos` lo llame (eliminando la lógica acumulativa duplicada), con el mismo
filtro de estado en los dos caminos.

---

## 6. El Elo por sanción puede quedar negativo — Baja ✅ Resuelto

> **Resuelto (26/11/2026):** las sanciones de ELO y SAFETY_RATING no bajan de 0 y guardan lo aplicado
> en `Sancion.cambioAplicado`; revertir devuelve eso (las viejas, sin el dato, revierten `valor`).
> Tests en `SancionEfectosIntegracionTest`.

**Dónde:** `service/SancionService.java:190` (`aplicarCambioElo`), igual en `aplicarCambioSafetyRating`

**Problema:** al cargar resultados, Elo y SR se limitan con `Math.max(0, …)`
(`ResultadoCarreraService.java:158-159`), pero las sanciones manuales no. Una sanción grande puede
dejar el Elo negativo.

**Ojo al resolverlo:** si se limita a 0, revertir la sanción ya no devuelve el valor original
(se resta lo pedido, no lo que se aplicó realmente). Habría que guardar el cambio efectivo en la
sanción o en `EloSancion` y revertir ese.

---

## 7. El check de la tabla de posiciones está en el controller — Deuda ✅ Resuelto

> **Resuelto (26/11/2026):** el cálculo cacheado pasó a `TablaPosicionesService.tabla`, y
> `CampeonatoService.getTabla(id, visor)` controla el acceso dentro de su transacción antes de
> llamarlo. Tests en `CampeonatoServiceTest`.

**Dónde:** `controller/CampeonatoController.java:59-62`

El control de acceso de `GET /api/campeonatos/{id}/tabla` se hace en el controller, sobre una
entidad desconectada, porque `getTabla` es `@Cacheable`. Hoy funciona porque `veContenido` solo
lee `visibilidad` y `admin.getId()` (que no inicializa el proxy), pero contradice la regla 3 de
`arquitectura-y-patrones.md` y se rompe con un `LazyInitializationException` apenas el check toque
otra relación.

**Solución propuesta:** un método de servicio `@Transactional` sin caché que haga el check y
después llame al `getTabla` cacheado a través de otro bean (o separar el cálculo cacheado en una
clase propia para que el proxy aplique).

---

## 8. Errores tragados en el frontend — Deuda ✅ Resuelto

> **Resuelto (26/11/2026):** los 56 `catchError(() => of(...))` pasaron a
> `ErrorCargaService.siFalla(...)` (consola siempre; toast único para status 0/5xx). Ver §4.4 de
> `arquitectura-y-patrones.md`. No se agregó un estado de error por panel en las plantillas.

Hay **56** `catchError(() => of(...))` en 10 componentes (`admin`, `categories`, `home`,
`incidents`, `my-championships`, `driver-profile`, `my-profile`, `race-detail`, `races-list`,
`setups`). Un 500 o un 403 se ven como "no hay datos". Está descrito en §4.4 de
`arquitectura-y-patrones.md` y fue lo que demoró el diagnóstico del bug de lazy loading.

**Solución propuesta:** un helper en `core/` que registre el error en una signal de error por
panel (o en `ToastService`) y devuelva el valor vacío, y reemplazarlo componente por componente.

---

## Otros

- `docs/patrones-diseno.md` tiene un cambio sin commitear que reemplaza `## 2. GRASP` por
  `### 2. Creator`; las secciones siguientes siguen numeradas `2.2`, `2.3`…, así que la jerarquía
  queda rota. También borra las secciones "Ausencias verificadas" y "Referencias". Confirmar si
  fue intencional.
- ✅ JSON de prueba con varios pilotos, uno doblado y un DNF:
  `testJsonServidor/2026_8_8_RACE_doblado_y_dnf.json`. Sirve también para el punto 3.
