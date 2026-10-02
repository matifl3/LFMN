# Patrones de diseño del sistema

Inventario de los patrones implementados en `lfmNacional/` (backend Spring Boot 4.1.0) y
`frontend/` (Angular 22), con referencia `archivo:línea` para cada afirmación.

**Cómo leer este documento.** Cada patrón está clasificado en una de tres categorías:

| Marca | Significado |
|---|---|
| **GoF** | Implementación fiel de *Design Patterns* (Gamma, Helm, Johnson, Vissots) |
| **Nombreugar** | Patrón descrito en la literatura de diseño (Fowler, Evans, Fowler & Evans), sin forma canónica única |
| **Analogía** | Se parece a un patrón pero **no lo es**; se aclara por qué |

La sección [8. Ausencias verificadas](#8-ausencias-verificadas) documenta lo que se buscó
y **no** existe. Es tan informativa como la lista de lo que sí existe.

Todas las líneas corresponden al commit `81728258` más los cambios de `sistemas-puntos`
del mismo trabajo.

---

## 1. Resumen

| # | Patrón | Ubicación | Tipo |
|---|---|---|---|
| 1 | Strategy + Simple Factory | `service/puntos/` | **GoF** |
| 2 | Chain of Responsibility | `service/inscripcion/` | **GoF** (variante) |
| 3 | Template Method | `service/sesion/Importador*` | **GoF** (con hook muerto) |
| 4 | Idempotencia por hash | `service/sesion/IdempotenciaSesionService` | Nombreugar |
| 5 | Registry por clave (2.º sitio) | `service/SesionServidorService` | Nombreugar |
| 6 | Dispatch por `switch` sobre enum | `LogroService`, `SancionService` | Analogía |
| 7 | Strategy con lambdas | `config/SecurityConfig` | **GoF** |
| 8 | Proxy dinámico | `@Cacheable` / `@Transactional` | **GoF** |
| 9 | Get-or-Create | 7 servicios | Analogía (a Null Object) |
| 10 | Null Object de colección vacía | `service/VueltaService` | **GoF** |
| 11 | Registry con TTL en memoria | `SteamService`, `RateLimitFilter` | Nombreugar |
| 12 | Command object | `ResultadoImportService`, `IncidenteService` | Analogía |
| 13 | Mapper / Assembler | todos los servicios | Nombreugar |
| 14 | Service Layer | 4 servicios densos | Analogía (a Facade) |
| 15 | Compensating action / undo | `SancionService`, `ApelacionService` | Analogía (a Saga) |
| 16 | Rate limit + lockout | `security/RateLimitFilter` | Nombreugar |
| 17 | Fail-soft por paso | `service/sesion/ImportadorRace` | Nombreugar |

---

## 2. Strategy + Simple Factory — `service/puntos/`

**GoF puro. Es el único Strategy del backend implementado con clases.**

```
SistemaPuntos (interfaz)
├── SistemaPuntosF1      tabla List.of(25, 18, 15, 12, 10, 8, 6, 4, 2, 1)
└── SistemaPuntosTop10   tabla List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
```

| Pieza | Ubicación |
|---|---|
| Contrato | `service/puntos/SistemaPuntos.java:3` — `clave()`, `nombre()`, `puntosPara(int)` |
| Impl. F1 | `service/puntos/SistemaPuntosF1.java:7-8`, tabla en `:10` |
| Impl. Top 10 | `service/puntos/SistemaPuntosTop10.java:7-8`, tabla en `:10` |
| Factory | `service/puntos/SistemaPuntosFactory.java:26` recibe `List<SistemaPuntos>` por inyección |
| Catálogo | `service/puntos/SistemaPuntosCatalogo.java:11` — record DTO |
| Endpoint | `controller/SistemaPuntosController.java:24-27` — `GET /api/sistemas-puntos` |

### Características que lo hacen correcto

**Descubrimiento automático por contenedor** (`SistemaPuntosFactory.java:26`). La factory
recibe la lista de beans por inyección; no hay `switch` ni `Map` estático. Agregar un
esquema nuevo es crear un `@Component implements SistemaPuntos` y nada más.

**Normalización de clave** (`SistemaPuntosFactory.java:66-71`): `trim()` +
`toLowerCase(Locale.ROOT)`. Por eso `"top 10"`, `"TOP 10"` y `"  Top 10 "` resuelven al
mismo bean.

**Fail-safe, no null** (`SistemaPuntosFactory.java:35`): clave desconocida, `null` o vacía
caen en `porDefecto()` en vez de devolver `null`. Esto importa porque los datos ya
guardados pueden traer claves viejas.

**Fail-fast de configuración** (`SistemaPuntosFactory.java:62-63`): `orElseThrow(
IllegalStateException)`. Si alguien borra `SistemaPuntosF1`, la app falla al arrancar en
vez de romper silenciosamente el cálculo de puntos a mitad de temporada.

**Strategy Total** (`SistemaPuntosF1.java:24-27`, `SistemaPuntosTop10.java:19-23`): ambas
implementaciones devuelven `0` fuera de rango en vez de lanzar excepción. El cliente nunca
necesita guarda de rango.

**El cliente no depende del producto concreto** (`CampeonatoService.java:41, 208, 213`):
inyecta el factory, llama `de(clave)` y usa `puntosPara(...)`. Nunca menciona
`SistemaPuntosF1`. Esa es la Hint del patrón.

### Tests que documentan el patrón

- `SistemaPuntosFactoryTest.java:29-36` — `de(null)`, `de("")`, `de("F1_SPRINT")` caen al default
- `SistemaPuntosFactoryTest.java:60-68` — falla al arrancar si desaparece la estrategia default
- `SistemaPuntosFactoryTest.java:48-58` — congela las claves canónicas que persiste el frontend
- El factory es testeable sin Spring: se instancia con `List.of(new SistemaPuntosF1(), new SistemaPuntosTop10())` (`:12-13`)

---

## 3. Chain of Responsibility — `service/inscripcion/`

**GoF puro en estructura, con una variante importante:** la cadena no se puede cortocircuitar
y el "fail" es una excepción.

| Eslabón | Archivo | `@Order` | Regla |
|---|---|---|---|
| 1 | `InscripcionesAbiertasValidador.java:14` | `@Order(1)` | Estado `PROGRAMADA`/`INSCRIPCIONES_ABIERTAS` + ventana de cierre de 5 min (`:16`, `:24`) |
| 2 | `PertenenciaValidador.java:14` | `@Order(2)` | `accesoService.puedeParticiparEnCarrera(...)` — campeonato privado |
| 3 | `RequisitosEloValidador.java:12` | `@Order(3)` | Elo dentro del rango de la categoría; **skip si el campeonato es `PRIVADO`** (`:16-18`) |

### El esqueleto

`ValidadorInscripcion.java:6` define el patrón:

```java
public final void validar(Carrera carrera, Usuario usuario) {   // :14
    validarPropio(carrera, usuario);
    if (siguiente != null) {
        siguiente.validar(carrera, usuario);
    }
}
protected abstract void validarPropio(Carrera carrera, Usuario usuario);  // :21
```

Cada subclase implementa solo `validarPropio`. El `final` en `validar` es lo que garantiza
que nadie rompa el avance de la cadena.

### Variante respecto del GoF canónico

El CoF clásico suele propagar el control de salida para cortocircuitar. Aquí **no hay
cortocircuito**: los tres eslabones se ejecutan siempre, en orden, y el rechazo es
`throw new BusinessException(...)`. Es un **pipeline de validación con fail-fast por
excepción**, no una cadena que pueda interrumpirse.

### El ensamblador re-construye en cada request

`InscripcionValidacion.java:23-36` (`encadenar()`) arma la cadena con doble puntero
`cabeza`/`anterior` **cada vez que se valida una inscripción** (`:17`). No hay caché del
encadenado.

> **Riesgo verificado**: eso escribe el campo mutable `siguiente` (`ValidadorInscripcion.java:8-12`)
> de beans singleton desde un camino con concurrencia de requests. Hoy es idempotente —
> siempre reconstruye la misma cadena por el mismo orden `@Order` — pero es estado compartido
> escrito concurrentemente sin sincronización. El campo podría ser `volatile` o la cadena
> construirse una sola vez.

### El orden importa semánticamente

Los `@Order` no son decorativos: hay tres eslabones y el orden define **qué error ve el
piloto primero**. Con el orden actual, un piloto sin Elo suficiente en un campeonato privado
recibe el mensaje de pertenencia (eslabón 2) y nunca llega al de Elo (eslabón 3), porque
`RequisitosEloValidador:16-18` retorna temprano para `PRIVADO`. Es intencional y correcto.

### Tests

`InscripcionValidacionTest.java:91-99` usa `verifyNoInteractions(accesoService)` para probar
que el eslabón 1 frena **antes** de llegar al 2 — es decir, que el `@Order` se respeta.

---

## 4. Template Method — `service/sesion/ImportadorSesion`

**GoF puro.**

```
ImportadorSesion (abstract)          ImportadorRace     ImportadorQualify
├── final importar()      :18        └── super("RACE")      :19-26
│     ├── validar()       :19  ← hook opcional (cuerpo vacío, :24-25)
│     ├── procesar()      :20  ← abstract :27
│     └── return tipo
├── final tipo()          :14
└── validar()             :24  hook
```

El dato de la subclase (`"RACE"` / `"QUALIFY"`) se empuja por constructor (`:10-12`).
`ImportadorRace.java:28-41` y `ImportadorQualify.java:18-21` implementan `procesar`.

### Hook declarado sin uso

> **Verificado**: `ImportadorSesion.validar()` (`:24-25`) **no lo overridea ninguna clase de
> producción**. El único resultado de `protected void validar(` en todo `src/main/java` es la
> declaración misma. Solo `ImportadorSesionTest.java:34-43` y `:46-55` lo ejercitan.
>
> El paso `validar` de `:19` no hace nada en el flujo real. No es un bug (el default vacío
> lo hace inofensivo), pero es un punto de extensión que promete una capacidad que no existe.

---

## 5. Idempotencia por hash SHA-256 — `service/sesion/IdempotenciaSesionService`

**Nombreugar** (robustez / idempotencia; sin forma canónica en la literatura de patrones).

| Pieza | Ubicación |
|---|---|
| Longitud de clave | `:22` `LONGITUD_CLAVE = 40`, aplicada en `:82` |
| Clave de sesión | `:28-41` — canonicaliza carrera + pista + config + tipo + duración + nº vueltas + **tamaños** de `cars`/`result`/`laps`/`events` |
| Clave de incidente | `:47-57` — granularidad por evento concreto |
| SHA-256 | `:78-86` `MessageDigest` + `HexFormat`, `NoSuchAlgorithmException → IllegalStateException` (`:83-85`) |

### Detalle de diseño: canonicalizar por tamaño, no por contenido

`:30-39` usa los **tamaños de colección** (`cars.size()`, `result.size()`, `laps.size()`,
`events.size()`), no el contenido. Es barato de calcular y estable ante reordenamientos.
El costo: dos sesiones con distinta distribución pero igual cantidad de eventos y las mismas
métricas pueden colisionar en la clave. Para el caso de uso (mismo JSON reenviado) es
correcto.

### Normalización semántica del ruido numérico

`:59-65` (`impacto()`) redondea el `impactSpeed` con `Math.round` para que `40.4` y `40.0`
— el mismo choque con ruido de punto flotante — colapsen a la misma clave, mientras `40` y
`41` siguen siendo incidentes distintos. El comentario `:61-63` documenta la intención.

### Los dos consumidores

- `service/SesionServidorService.java:50-55` — calcula la clave, busca `findByClave(clave)`;
  si existe devuelve `ResultadoImportacion(..., yaProcesada=true)` sin reprocesar. El 409
  que ve el cliente viene de la constraint `UNIQUE` en BD si corre concurrente.
- `service/IncidenteAutoGenService.java:48-52` — `claveIncidente(...)` + `existsByClaveOrigen(...)`.

`IncidenteAutoGenService.java:46-47` documenta explícitamente la intención de retry
("reintentos de ingesta no deben duplicar incidentes"), aunque **el retry no exista todavía**
(ver [sección 8](#8-ausencias-verificadas)).

---

## 6. Registry por clave — el segundo sitio de dispatch

`service/SesionServidorService.java:30, 62-72`

```java
private final List<ImportadorSesion> importadores;   // :30
...
String tipo = sesion.type().toUpperCase();            // :66
ImportadorSesion importador = importadores.stream()
        .filter(i -> i.tipo().equals(tipo))
        .findFirst()
        .orElseThrow(() -> new BusinessException(...)); // :67-70
```

**Misma forma que `SistemaPuntosFactory`**, con dos diferencias:

| | `SistemaPuntosFactory` | `SesionServidorService` |
|---|---|---|
| Política ante clave desconocida | `orElseThrow` → fallback al default (`SistemaPuntosFactory.java:35`) | `orElseThrow` → error al cliente (`SesionServidorService.java:69`) |
| Tipo del producto | Interfaz | Clase abstracta |
| Expone catálogo | Sí (`disponibles()`, `:44-52`) | No |

El fallback de uno y el error del otro es **intencional y correcto**: un sistema de puntaje
desconocido no debe romper un campeonato ya en curso, pero un tipo de sesión no soportado
debe avisar al operador.

---

## 7. Patrones estructurales

### 7.1 Dispatch por `switch` sobre enum — **Analogía, no GoF**

Tres switches en `service/`, con dos políticas de exhaustividad **distintas**:

**a) `LogroService.java:180-188`** — switch expression con 7 brazos, **sin `default`**:

```java
return switch (condicion) {
    case VICTORIAS -> resultadoCarreraRepository.countByUsuario_IdAndPosicionFinal(usuarioId, 1);
    case PODIOS -> ...countByUsuario_IdAndPosicionFinalLessThanEqual(usuarioId, 3);
    case CARRERAS, POLES, VUELTAS_RAPIDAS, CARRERAS_COMPLETADAS, ELO -> ...
};
```

`TipoCondicionLogro` tiene exactamente 7 constantes y hay 7 brazos → **agregar una constante
al enum rompe la compilación**. Eso es correcto: el compilador te obliga a decidir.

**b) `SancionService.java:147-156` y `:175-184`** — 4 casos cada uno, **con `default -> {}`
no-op**, y son un par espejado (`aplicarEfectos` / `revertirEfectos`).

> **Riesgo verificado**: `TipoSancion` tiene 7 constantes (`PUESTOS, SEGUNDOS,
> DRIVE_THROUGH, STOP_AND_GO, DESCALIFICACION, ELO, SAFETY_RATING`) pero solo 4 tienen
> efecto numérico. `DRIVE_THROUGH`, `STOP_AND_GO` y `DESCALIFICACION` caen al `default`
> vacío. Hoy es correcto —esas sanciones no modifican un número— pero el `default` silencioso
> significa que un `TipoSancion` nuevo se aplicaría sin efecto y sin warning.
>
> La asimetría con `LogroService` es el punto: uno deja que el compilador fuerce la decisión,
> el otro la esconde.

**Por qué no es Visitor**: `TipoCondicionLogro` no tiene comportamiento (solo constantes,
sin campos ni métodos) y el dispatch es simple, no doble. Es la variante "poor man's
Strategy": una tabla de estrategias escrita como `switch` en vez de clases.

### 7.2 Strategy con lambdas — `config/SecurityConfig.java:88, 94`

**GoF puro**, con las interfaces prestadas por Spring Security:

```java
.authenticationEntryPoint((request, response, ex) -> { /* 401 + JSON */ })   // :88-93
.accessDeniedHandler((request, response, ex) -> { /* 403 + JSON */ })       // :94-99
```

Comportamiento intercambiable pasado en el punto de uso, sin subclase nombrada.

### 7.3 Proxy dinámico — `@Cacheable` / `@Transactional`

**GoF puro**, aplicado por el contenedor de Spring, no escrito a mano.

13 `@Cacheable` y 27 `@CacheEvict` en `service/`: 7 servicios cachean y 2 más solo evacuan una
caché ajena. Los conteos son de anotaciones reales, no incluyen menciones dentro de javadoc
(`CampeonatoService.java:186` nombra `@Cacheable` en un comentario, no es una anotación):

| Servicio | Cachés | Evict en |
|---|---|---|
| `AnuncioService` | `anuncios`, `anuncio_ultimo` (`:30`, `:35`) | `:44`, `:55`, `:61` |
| `ArchivoCarreraService` | `archivos_carrera` (`:42`, `:48`) | `:54`, `:77` |
| `CampeonatoService` | `tabla_posiciones` (`:192`) | `:100`, `:121`, `:163`, `:172`, `:201` |
| `CategoriaService` | `categorias`, `categorias_elo` (`:28`, `:33`, `:38`) | `:46`, `:64`, `:78` |
| `LogroService` | `logros` (`:53`, `:59`) | `:65`, `:81`, `:96` |
| `RecompensaService` | `recompensas` (`:35`, `:41`) | `:53`, `:64`, `:73` |
| `UsuarioService` | `usuarios` (`:161`) | `:169`, `:241`, `:270` |
| `ResultadoCarreraService` | — (no cachea) | `:87`, sobre `usuarios` |
| `SancionService` | — (no cachea) | `:68`, `:78`, `:111`, `:170`, sobre `usuarios` |

**La prueba documental más fuerte de que el Proxy importa** está en
`CampeonatoService.java:185-190`, cuyo comentario explica que el gate de membresía *no puede*
vivir dentro del método porque *"con `@Cacheable` el cache se consulta antes de ejecutar el
cuerpo"*. El autor está razonando explícitamente sobre la interposición del proxy. El mismo
razonamiento aparece en `CarreraService.java:67-71` y `:110-112`.

> **Bug corregido (`7034242c`) — la caché `usuarios` quedaba sin evictar**:
> `UsuarioService.listAllBasico()` cachea en `usuarios` (`:161-162`) un `UsuarioBasicoResponse`
> con exactamente 5 campos: `id`, `nombrePiloto`, `fotoPerfil`, `elo`, `safetyRating`. El único
> `@CacheEvict` que había sobre esa clave estaba en `delete` (`:270`), así que cualquier
> escritura de esos 5 campos dejaba el listado público de pilotos con datos viejos.
>
> El alcance real era **más ancho** que los 4 métodos de `UsuarioService` sospechados al
> principio, y **más chico** en un punto. `cambiarRol` (`:224`, escribe solo `rol`) y
> `updateHabilitado` (`:231`, escribe `habilitado` y `tokenVersion`) no tocan ningún campo
> cacheado, así que no necesitan evict. Los que sí escribían datos cacheados eran 5 entradas
> públicas de 3 clases:
>
> | Método | Qué ensucia la caché |
> |---|---|
> | `UsuarioService.updatePerfil` (`:170`) | `nombrePiloto` |
> | `UsuarioService.updateRating` (`:242`) | `elo`, `safetyRating` |
> | `ResultadoCarreraService.cargarResultados` (`:88`) | elo y safety rating de **todos** los pilotos de la carrera, vía `recalcularEloYSafetyRating` (`:116`) |
> | `SancionService.create` (`:69`) / `update` (`:79`) / `delete` (`:112`) | elo y safety rating, al aplicar o revertir efectos |
> | `SancionService.revertirEfectos` (`:171`) | ídem, desde el único llamador externo (`ApelacionService`) |
>
> Los de `ResultadoCarreraService` y `SancionService` eran los que más dolían:
> `cargarResultados` es la operación más frecuente del sistema y dejaba el ranking público
> entero desactualizado.
>
> **Trampa del proxy, documentada en el código**: `aplicarEfectos` (`:145`) y
> `revertirEfectos` (`:171`) son el par aplicar/revertir, pero solo el método público puede
> llevar `@CacheEvict` — los privados no pasan por el proxy de Spring. Y `update()`/`delete()`
> llaman a `this.revertirEfectos(...)` desde su propio cuerpo, que es self-invocation y
> tampoco pasa por el proxy: por eso `update` y `delete` llevan su propio evict, y el de
> `revertirEfectos` únicamente cubre el llamador externo.
>
> `LogroService` no está afectado: su caché `logros` guarda las definiciones, no el progreso
> por usuario, y `listarLogrosUsuario()` no está cacheado.
>
> **Nota sobre esta tabla**: los conteos anteriores (12 y 23) y el alcance de los "4
> mutadores" estaban mal aun antes del fix, por contraste con el código. Verificado contra
> las anotaciones reales con `Select-String` sobre `service/`, no estimado a ojo.

**Advertencia estructural**: por ser proxies, `this.metodo()` dentro de la misma clase **no**
pasa por el proxy, así que `@Cacheable`/`@Transactional` se ignoran en llamada interna. Revisé
los candidatos cross-bean (`SetupCalificacionService:52`, `RecompensaService:56`,
`ApelacionService:63,82`, `IncidenteService:157`) y todos son llamadas a **otros** beans, así
que no encontré un self-invocation que rompa. No es bug: es advertencia.

### 7.4 Get-or-Create — 7 sitios · **Analogía a Null Object**

Construir la entidad por defecto en el sitio en vez de trabajar con `null` u
`Optional.empty()`:

| Ubicación | Patrón |
|---|---|
| `LogroService.java:139-140` | `getOrDefault(logro.getId(), UsuarioLogro.builder().progreso(0)...)` |
| `LogroService.java:156-162` | ídem, con `.logro().usuario()` |
| `ClasificacionImportService.java:52-57` | `orElseGet(() -> SesionClasificacion.builder()...)` |
| `ResultadoCarreraService.java:101-104` | `orElseGet(() -> ResultadoCarrera.builder()...)` |
| `SetupCalificacionService.java:44-49` | `orElseGet(() -> SetupCalificacion.builder()...)` |
| `IncidenteService.java:174-179` | `orElseGet(() -> VotoComisario.builder()...)` |
| `CampeonatoService.java:214-224` | `orElseGet(() -> CampeonatoPosicion.builder().puntos(0).posicion(count+1))` |

**Por qué no es Null Object de GoF**: Null Object exige *una subclase que representa la
ausencia y absorbe las operaciones sin efecto*. Acá la instancia por defecto es **una entidad
real del mismo tipo que se persiste después** (`LogroService:170`,
`ResultadoCarreraService:116`, `CampeonatoService:228`). El nombre correcto es
**Get-or-Create / upsert**. Que aparezca en 7 lugares lo vuelve un patrón deliberado del
proyecto, no un accidente.

### 7.5 Null Object de colección vacía — `service/VueltaService.java` · **GoF legítimo**

`:69`, `:79-82`, `:85`, `:87` usan `getOrDefault(uid, Map.of())` y `getOrDefault(lap,
List.of())` en vez de `null`. `Map.of()` / `List.of()` son **inmutables y vacíos**: absorber
llamadas de lectura sobre ellos es seguro. Es Null Object de verdad, aplicado a colecciones.
`:82`, `:88` usan `Long.MAX_VALUE` como valor centinela.

### 7.6 Registry con TTL en memoria

**`service/SteamService.java`** — códigos de un solo uso para vincular Steam:

| Pieza | Ubicación |
|---|---|
| Registro | `:62` `private final Map<String, CodigoAuth> codigosAuth = new ConcurrentHashMap<>()` |
| Record de valor + expiración | `:36-37` `record CodigoAuth(Long usuarioId, long expiraEn)` |
| Alta | `:154-156` limpia expirados, luego `put` con `UUID.randomUUID()` |
| Consumo one-shot | `:165` `codigosAuth.remove(codigo)` → get-and-delete **atómico** |
| Evicción perezosa | `:178-181` `entrySet().removeIf(...)` |

El `remove` en `:165` hace que el código no pueda reutilizarse aunque sea entre hilos: es
correcto para un token de un solo uso.

**`security/RateLimitFilter.java:24`** — mismo patrón para contadores anti-brute-force.

> **Riesgo verificado — estado en memoria con múltiples réplicas**: `SteamService:62` y
> `RateLimitFilter:24` son `ConcurrentHashMap` **en memoria de proceso**. En Render, si el
> servicio escala a más de una instancia, un código de Steam emitido en la instancia A no se
> puede completar en la B, y el rate limit se multiplica por el número de instancias. Ambos
> deberían usar Redis (que **no** está en `pom.xml`). Hoy el servicio es de una sola instancia
> y funciona; es una restricción de escala, no un bug actual.

> `RateLimitFilter` además nunca hace eviction de IPs que dejaron de intentar: los contadores
> quedan en el mapa de forma no acotada (`:63` solo inserta).

### 7.7 Command object — **Analogía**

Los DTO `record` cumplen el rol de *Command* (encapsular una petición y pasarla a un
receptor):

- `ResultadoImportService.java:57-106` arma `List<ResultadoCarreraRequest>` y lo entrega
  como un solo comando a `resultadoCarreraService.cargarResultados(...)` (`:106`)
- `IncidenteService.java:145-158` reconstruye un `SancionRequest` por cada sanción de una
  resolución (`:147-156`) y emite **N comandos** derivados de un request (`:157`)

**Por qué analogía**: son DTOs de API (`dto/**`) reutilizados internamente como comandos,
sin `execute()` ni receiver ni `undo()` propios.

### 7.8 Mapper / Assembler — **Nombreugar**

Cada servicio tiene un `private XxxResponse toResponse(...)` que cumple dos funciones: mapear
**y** desacoplar el grafo JPA lazy del JSON. Esto último es obligatorio, no decorativo —
serializar una entidad con `@ManyToOne(LAZY)` fuera de sesión lanza `LazyInitializationException`.

Ejemplos: `LogroService.java:224-254` (3 mappers distintos: `toResponse`, `toRecompensaResponse`,
`toUsuarioLogroResponse`), `UsuarioService.java:357-370`,
`VueltaService.java:41-55`, `SetupService.java:201-216`, `EstadisticasService.java:40-66`.

Dos casos que no pueden ser método local usan el helper estático compartido
`mapper/EntityMapper.java:20-26` (`resolveCarreraInfo`) y `:28-34` (`resolveUsuarioBasico`),
que además resuelven el caso `null`.

### 7.9 Service Layer — **Analogía a Facade**

No es Facade de GoF (que colapsa un subsistema tras una interfaz única): es Service Layer /
Transaction Script que orquesta muchos repositorios.

| Servicio | Colaboradores | Rol |
|---|---|---|
| `ResultadoCarreraService.java:88-122` | 11 (`:37-47`) | **el orquestador más denso**: valida → guarda de idempotencia (`:91-94`) → upsert N resultados (`:96-113`) → `recalcularEloYSafetyRating` (`:116`) → `actualizarPuntos` (`:117`) → `evaluarLogros` por piloto (`:119`). Todo en **una sola `@Transactional`** (`:86`) |
| `UsuarioService.java:267-307` | 23 (`:49-72`) | borrado en cascada de **4 fases** con el orden documentado en comentarios: referencias directas (`:272-277`), dependencias de agregados (`:279-286`), agregados (`:288-290`), filas propias (`:292-302`), más `desvincularAdmin` (`:305`) para no dejar la FK colgando |
| `EstadisticasService.java:27-37`, `:40-66` | 11 repos | façade read-only que agrega **23 contadores** en un DTO |
| `SancionService.java:28-37` | 10 | `create` en 3 fases: `buildSancion` → `aplicarEfectos` → `notificar` (`:69-75`) |

`ResultadoCarreraService.java:131-134` toma un **snapshot** de elos en `Map<Long,Integer>`
antes de recalcular, para que el cálculo no lea valores ya modificados en el mismo lote.

### 7.10 Compensating action / undo — **Analogía a Saga**

`SancionService.aplicarEfectos` (`:145-157`) tiene su espejo exacto `revertirEfectos`
(`:155-169`), con guarda de idempotencia por el flag `efectosAplicados` (`:156-158`) y fila de
auditoría negativa con prefijo "Reversion de sancion" (`:244-245`, `:257-258`).

El par de métodos privados es simétrico: `aplicarCambioElo` (`:171-181`) /
`revertirCambioElo` (`:221-232`), y así los tres pares restantes.

`ApelacionService.java:81-83` cierra el flujo de compensación de 3 saltos: **sanción →
apelación aprobada → undo de efectos**.

`update` (`:87-89`, `:101-103`) y `delete` (`:117`) revierten **antes** de mutar y reaplican
después, solo si `afectaEfectos` (`:81-86`).

**Por qué no es Saga**: todo ocurre dentro de una única `@Transactional` JPA, así que hay
rollback transaccional. La compensación existe para **efectos ya materializados en filas de
negocio** (elo aplicado en la tabla de usuarios, posiciones modificadas en resultados), que
el rollback de la transacción de la fila de sanción no deshace.

### 7.11 Rate limit + lockout — `security/RateLimitFilter.java`

**Nombreugar (resiliencia). NO es un circuit breaker.**

| Pieza | Ubicación |
|---|---|
| Constantes | `:20-22` `MAX_INTENTOS=5`, `VENTANA_MILIS=60_000`, `LOCKOUT_MILIS=300_000` |
| Registro en memoria | `:24` `Map<String, IntentoCuenta> intentosPorIp` |
| Instancia vacía por defecto | `:26-31` `record IntentoCuenta(...)` con factory method `vacia()` en `:28-30` (otro Get-or-Create) |
| Ventana deslizante | `:68-70` purga marcas viejas antes de contar |
| Corte + lockout | `:71-75` resetea la ventana y calcula `bloqueadoHasta = ahora + LOCKOUT` |
| Serialización | `:64` `synchronized (cuenta)` — el record es mutable |
| **Guarded Call** | `:43` `return` **sin** llamar `filterChain.doFilter` → cortocircuita la cadena |
| IP real | `:81-87` detrás de `X-Forwarded-For` |
| Registro en la cadena | `SecurityConfig.java:100-101` (`addFilterBefore`) |

**Por qué no es circuit breaker**: no hay máquina de estados closed/open/half-open, ni
detector de fallos, ni fallback. Cuenta intentos por IP, no fallas de una dependencia.

### 7.12 Fail-soft por paso — `service/sesion/ImportadorRace.java:31-40`

Dos `try/catch` que **tragan la excepción** y solo loguean `warn` (vueltas en `:31-35`,
incidentes en `:36-40`), mientras el paso crítico `resultadoImportService.importarResultados`
(`:30`) queda sin proteger.

Es "best-effort por paso": si los incidentes fallan, la importación de resultados sigue.

**Por qué NO es retry**: no hay segunda ejecución, ni backoff, ni contador de intentos. El
mecanismo de idempotencia de la [sección 5](#5-idempotencia-por-hash-sha-256--servicesesionidempotenciasesionservice)
deja la puerta abierta para agregarlo, pero hoy no existe.

### 7.13 Dependencia ausente con fallback — `service/RecuperarPasswordService.java`

`:35` `ObjectProvider<JavaMailSender>`, `:57-61` rama de degradación: si falta SMTP, loguea
y sigue (imprime el link en el log) en vez de romper.

**No es Null Object de GoF** — no hay objeto nulo que absorba llamadas. Es *Optional
Dependency* con rama explícita. El contraste es instructivo: el mismo servicio **sí** lanza
`BusinessException` si el usuario no existe (`:87-88`), así que la ausencia de SMTP y la
ausencia de usuario tienen tratamientos opuestos, a propósito.

### 7.14 Singleton

Los ~40 servicios son singletons **del contenedor** por defecto de Spring (`@Service` +
`@RequiredArgsConstructor` + campos `private final`). Cero `getInstance()`, cero holder classes,
cero singletons "a la GoF" — el patrón clásico no se usa.

Dos clases utilitarias con constructor privado (anti-instanciación, Utility Class):
`mapper/EntityMapper.java:6-8` y `util/FileUtil.java:3-6`. `FileUtil.obtenerExtension` se
reusa en 3 servicios (`ImagenService:38`, `SetupService:146`, `ArchivoCarreraService:59`).

### 7.15 Reglas transversales

No son patrones, pero son convenciones consistentes en todo el código:

- **Guardas fail-closed**: `getEntity(...)` con `orElseThrow(ResourceNotFoundException)` en
  25+ servicios. Excepción tipada en vez de `Optional` vacío. El único `Optional` que vuelve
  al controller es `CarreraService.java:261-263` con `orElse(false)`.
- **Constantes de política en un solo lugar**: `InscripcionesAbiertasValidador.java:16` es la
  fuente, re-exportada por `InscripcionService.java:27`.

  > **Duplicación verificada**: `CarreraService.java:37` re-declara
  > `private static final int MINUTOS_CIERRE_PREVIO = 5;` **como literal**, y lo usa en su
  > `@Scheduled` de cierre (`:182`). Si cambia en el validador, el cierre automático se
  > desincroniza de la validación. Tres referencias, dos fuentes de verdad.

- **Timestamps de negocio inyectados, no leídos dentro del dominio**: `LocalDateTime.now()`
  explícito en `IncidenteService:85`, `:182`, `ClasificacionImportService:58`,
  `SancionService:141`.
- **Índices anti-N+1**: `Map` como lookup en `CarreraService:249-255` (reutilizado en 4
  métodos públicos), `ClasificacionImportService:67-77`, `VueltaService:63-71` (doble índice
  con `computeIfAbsent`), `IncidenteService:100-101`, `LogroService:133-136` y `:149-152`.
- **Vuelta numerada en un paso**: `VueltaImportService:43`
  `numeros.merge(guid, 1, Integer::sum)`.

---

## 8. Ausencias verificadas

Se buscó cada patrón en `src/main/java` y en `pom.xml`. **Todos estos dan cero resultados:**

| Patrón | Resultado |
|---|---|
| **Retry** (`spring-retry`, `@Retryable`, `RetryTemplate`) | no existe |
| **Circuit Breaker** / Bulkhead / TimeLimiter (resilience4j) | no existe |
| **Observer / eventos** (`ApplicationEventPublisher`, `@EventListener`, `ApplicationListener`) | no existe |
| **Iterator / Iterable** propios | no existe — toda iteración es `for`/`stream` |
| **Decorator** propio (una clase que envuelve a otra del proyecto) | no existe |
| **Adapter** propio | no existe — el rol lo juega Spring (`PasswordEncoder` ← `BCryptPasswordEncoder` en `AppConfig:11-14`) |
| **Null Object** real (subclase nula que absorbe operaciones) | no existe — hay instancias-por-defecto ([7.4](#74-get-or-create--7-sitios--analogía-a-null-object)) y centinelas, pero ninguna clase Null Object |
| **Dispatch por `instanceof`** | **0 ocurrencias** en todo `src/main/java` |
| **Composite** | no existe — la cadena de inscripción es lineal, no un árbol |
| **Bridge / Mediator / Memento / Interpreter** | no existen |
| **State** (máquina de estados explícita) | no existe — los estados son enums **datos** (`EstadoCarrera`, `EstadoIncidente`, `EstadoCampeonato`, `EstadoApelacion`, `EstadoInscripcion`, verificados: todos sin comportamiento) y las transiciones son `if`/asignaciones |
| **Visitor** | no existe — ni de objetos ni de enum ([7.1](#71-dispatch-por-switch-sobre-enum--analogía-no-gof) es `switch`) |
| **Strategy con clases** fuera de `service/puntos/` | no existe — `implements` en todo `service/` da **2 resultados**, ambos de `puntos` |
| **Command** con receiver/undo propio | no existe — solo DTOs ([7.7](#77-command-object--analogía)) |
| **MapStruct** | no existe — el mapeo es a mano |

### La ausencia con más consecuencias: no hay Observer, y el fan-out de notificaciones está duplicado

`NotificacionService` **solo lo usan los controllers** — las 10 llamadas están en
`NotificacionController.java:29, 35, 39, 44, 49, 54, 60, 64, 69, 76, 80`.

Los **6 sitios internos** que generan notificaciones escriben el repositorio
**directamente**, duplicando el `builder()`:

| Ubicación | Contexto |
|---|---|
| `LogroService.java:206-213` | `notificarLogro` |
| `LogroService.java:215-222` | `notificarRecompensa` |
| `SancionService.java:306-313` | sanción aplicada |
| `ApelacionService.java:93-106` | apelación resuelta |
| `CampeonatoMiembroService.java:96-105` | piloto agregado |
| `CarreraService.java:214-222`, `:239-245` | cambios de estado de carrera |

No se puede reutilizar `NotificacionService.create(...)` internamente porque su firma toma el
DTO de API (`NotificacionService.java:49` `create(NotificacionRequest)`), no una entidad de
dominio.

**El idioma natural aquí sería Observer con `ApplicationEventPublisher`, o un `Notificador`
interno con firma de dominio. No existe ninguno de los dos.** El coste real: agregar un
tipo de notificación obliga a editar 6 lugares, y es fácil que uno se quede atrás.

---

## 9. Resumen de hallazgos

### Verificados y funcionales

1. Strategy + Factory de `service/puntos/` es el patrón mejor construido del proyecto:
   auto-descubrimiento, normalización, fail-safe, fail-fast de config, catálogo expuesto, y
   tests que congelan las claves.
2. La Chain of Responsibility de inscripción tiene 3 eslabones con orden semánticamente
   correcto y un test que verifica que el orden se respeta.
3. La idempotencia por hash SHA-256 con normalización de ruido flotante está bien resuelta
   para el caso de uso real (reenvío del mismo JSON).

### Verificados y problemáticos

| # | Hallazgo | Ubicación | Impacto |
|---|---|---|---|
| 1 | **Caché de usuarios sin evictar — CORREGIDO en `7034242c`** | `UsuarioService.java:161` vs `:169`, `:241`, `:270`; `ResultadoCarreraService.java:87`; `SancionService.java:68`, `:78`, `:111`, `:170` | Era peor de lo reportado: no eran 4 métodos de `UsuarioService` sino 5 entradas en 3 clases, y `cargarResultados` dejaba el ranking entero viejo. Ver [sección 7.3](#73-proxy-dinámico--cacheable--transactional) |
| 2 | **Constante `MINUTOS_CIERRE_PREVIO` duplicada** | `InscripcionesAbiertasValidador:16` vs `CarreraService:37` | El cierre automático puede desincronizarse de la validación |
| 3 | **Estado mutable en singleton escrito concurrentemente** | `ValidadorInscripcion.java:8-12`, reconstruido en `InscripcionValidacion:17` | Hoy idempotente; hoy es seguro por suerte, no por diseño |
| 4 | **`switch` con `default` no-op oculta casos** | `SancionService.java:154`, `:180` | Un `TipoSancion` nuevo se aplicaría sin efecto y sin warning |
| 5 | **Fan-out de notificaciones duplicado en 6 sitios** | ver [sección 8](#la-ausencia-con-más-consecuencias-no-hay-observer-y-el-fan-out-de-notificaciones-está-duplicado) | Agregar un tipo de notificación exige editar 6 lugares |
| 6 | **Hook `validar` sin ninguna implementación** | `ImportadorSesion.java:24-25` | Promete un punto de extensión que no existe |
| 7 | **Estado en memoria no escala a múltiples réplicas** | `SteamService:62`, `RateLimitFilter:24` | Restricción de escala en Render |
| 8 | **`RateLimitFilter` sin eviction** | `RateLimitFilter.java:63` | El mapa de IPs crece de forma no acotada |
| 9 | **9 colecciones `@OneToMany` sin uso en `Carrera`** | `Carrera.java:64-93` | `cascade = REMOVE` sobre 9 tablas sin control; −36 LOC si se quitan |

### Corrección al análisis previo

Este documento **corrige un error del análisis anterior**. Al recorrer `service/inscripcion/`
con un glob `*Inscrip*.java` se concluyo que la cadena tenía **un solo** validador
(`InscripcionesAbiertasValidador`) y que el `@Order` era decorativo. **Era falso**: la
búsqueda por `extends ValidadorInscripcion` revela 3 implementaciones
(`InscripcionesAbiertasValidador`, `PertenenciaValidador`, `RequisitosEloValidador`). Los
dos últimos no matchean el glob porque no llevan "Inscrip" en el nombre. El `@Order` define
el orden real de validación y ese orden es semántico.

---

## 10. Referencias

| Documento | Contenido |
|---|---|
| `docs/flujos.md` | Flujos funcionales |
| `docs/formulas-rating.md` | Fórmulas de Elo y Safety Rating |
| `docs/analisis-mejoras.md` | Análisis previo de mejoras |

### Advertencia sobre los números de línea

Este documento cita líneas concretas, y **esas referencias se pudren**. Cada cambio de código
las corre y nadie las actualiza salvo que se esté editando la sección justo ahí.

Evidencia medida, no supuesto:

- Al corregir la sección 7.3 (commit `7034242c`) hubo que recalcular 14 referencias de
  `SancionService`, `ResultadoCarreraService` y `UsuarioService`. Todas quedaron verificadas
  una por una contra el archivo.
- Antes incluso de ese commit ya había drift: este documento decía
  `UsuarioService.java:355-368` para `toResponse` (real `:357-370`), y
  `analisis-mejoras.md:89` dice `:230-246` para los mismos dos mappers de historial. Los tres
  números no coinciden entre sí ni con el código.
- Los conteos de la sección 7.3 (12 `@Cacheable`, 23 `@CacheEvict`) estaban mal **aun sin
  haber tocado una línea de caché**: los reales son 13 y 27.

Las afirmaciones cualitativas de cada patrón siguen siendo válidas. Lo que no es confiable sin
reverificar es la posición exacta de una línea. Para citar líneas, tomarlas del código en el
momento, no de acá.
| `docs/plan-produccion.md` | Plan de producción (histórico) |