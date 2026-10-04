# Patrones de diseño: GRASP y GoF

Inventario **exclusivo de los dos catálogos pedidos**: los 9 patrones de **GRASP**
(*Applying Patterns and Principles in Object-Oriented Design*, Evans) y los patrones de
**GoF** (*Design Patterns*, Gamma-Helm-Johnson-Vissots). Todo lo que no pertenece a alguno de
esos dos catálogos queda fuera por decisión de alcance.

**Convención de referencias.** Cada afirmación cita `Archivo.java` más el **nombre del
método**, no números de línea. Las líneas se pudren con cada refactor y nadie las actualiza;
los nombres de método sobreviven. Los conteos de anotaciones sí están medidos hoy y se
declaran como tales.

---

## 1. Resumen

### GRASP

| # | Patrón | Dónde vive | Veredicto |
|---|---|---|---|
| 1 | **Creator** | `SistemaPuntosFactory.de()` | Correcto: descubre y crea la estrategia |
| 2 | **Information Expert** | `SistemaPuntos.puntosPara()`, `EntityMapper`, `CampeonatoAccesoService` | Parcial: los expertos son los servicios, no las entidades |
| 3 | **Controller** | 31 clases `@RestController` + cadena de filtros de Spring Security | Correcto, asistido por framework |
| 4 | **High Cohesion** | `CampeonatoAccesoService`, `EntityMapper`, `VueltaService` | Correcto |
| 5 | **Low Coupling** | interfaces `SistemaPuntos`/`Repository`, DTOs `record`, `ApiService.normalizeList` | Correcto |
| 6 | **Polymorphism** | 2 `implements SistemaPuntos`, 3 `extends ValidadorInscripcion`, 2 `extends ImportadorSesion` | Correcto y sin excepciones |
| 7 | **Pure Accumulator** | `VueltaService.analisisCarrera()`, `VueltaImportService.importar()` | Correcto: evita recomputar y N+1 |
| 8 | **Pure Function** | `SistemaPuntosFactory.normalizar()`, `EntityMapper.*`, `VueltaService.minSector()` | Correcto |
| 9 | **Protected Variations** | factory de puntos, cadena de validadores, proxies de Spring | **Parcial**: se rompe con las notificaciones |

### GoF

| Familia | Patrón | Dónde vive | Nota |
|---|---|---|---|
| Creacional | **Simple Factory** | `SistemaPuntosFactory` | GoF canónico |
| Creacional | **Singleton** | 37 `@Service` | Intención cumplida, mecanismo distinto (contenedor) |
| Conductual | **Strategy** | `SistemaPuntosF1`, `SistemaPuntosTop10` | El único con clases; el resto del proyecto usa lambdas |
| Conductual | **Chain of Responsibility** | `ValidadorInscripcion` + 3 eslabones | Variante sin cortocircuito |
| Conductual | **Template Method** | `ImportadorSesion` + 2 subclases | GoF puro |
| Estructural | **Proxy** | `@Cacheable`, `@CacheEvict`, `@Transactional` | 14 / 27 / 168 ocurrencias |
| Estructural | **Null Object** | `VueltaService.analisisCarrera()` (`Map.of()`, `List.of()`) | Aplicable a colecciones |

**Lo que se eliminó de la versión anterior de este documento** por no ser GRASP ni GoF:
idempotencia por hash, registry por clave, registry con TTL, dispatch por `switch` sobre
enum, get-or-create, command object, mapper/assembler, service layer, compensating action,
rate limit, fail-soft por paso, singleton "a la GoF" con clase privada, y las reglas
transversales. Varios eran buenos hallazgos, pero describen convenciones del proyecto, no
patrones de estos dos catálogos.

---

## 2. GRASP

GRASP no son clases sino **responsabilidades que hay que ubicar**. Los nueve se verifican
uno por uno abajo, con los que no se cumplen marcados como tales.

### 2.1 Creator

> *Ubica la creación de una clase A en la clase B que usa A, mantiene datos de A o usa A de
> forma cercana.*

**`SistemaPuntosFactory.de(clave)`** es el Creator canónico del proyecto. Recibe
`List<SistemaPuntos>` por inyección del contenedor, normaliza la clave y devuelve la
instancia que corresponde:

```java
private final List<SistemaPuntos> estrategias;

public SistemaPuntos de(String clave) {
    String normalizada = normalizar(clave);
    for (SistemaPuntos estrategia : estrategias) {
        if (normalizar(estrategia.clave()).equals(normalizada)) {
            return estrategia;
        }
    }
    return porDefecto();
}
```

Cumple las cuatro condiciones del patrón: usa el producto de forma cercana, no guarda datos
de él, y **la instanciación no ocurre en el cliente**. `CampeonatoService` llama
`factory.de(campeonato.getSistemaPuntos())` y nunca menciona `SistemaPuntosF1`.

Tres decisiones que hacen que el Creator sea robusto:

- **Normalización de clave** (`normalizar()`): `trim()` + `toLowerCase(Locale.ROOT)`, con
  `null` → `""`. Por eso `"top 10"`, `"TOP 10"` y `"  Top 10 "` resuelven al mismo bean. Es
  además una Pure Function (§2.8).
- **Fail-safe, no null** (`de()`): clave desconocida, `null` o vacía caen en `porDefecto()`.
  Importa porque los datos ya guardados pueden traer claves viejas.
- **Fail-fast de configuración** (`porDefecto()`): `orElseThrow(IllegalStateException)`. Si
  alguien borra `SistemaPuntosF1`, la app **falla al arrancar** en vez de romper el cálculo
  de puntos a mitad de temporada.

Agregar un esquema de puntaje es crear un `@Component implements SistemaPuntos`. No hay que
tocar el factory ni el frontend, que consume `disponibles()`.

### 2.2 Information Expert

> *Pone el comportamiento en la clase que tiene los datos necesarios para calcularlo.*

Los tres expertos legítimos del proyecto:

**`SistemaPuntos.puntosPara(int posicion)`** — el tabla vive en la implementación y la
consulta también. Ningún servicio tiene los números hardcodeados; agregar un esquema nuevo no
obliga a tocar a quien calcula los puntos del campeonato.

**`EntityMapper.resolveUsuarioBasico()` / `resolveCarreraInfo()`** — el mapeo de entidades
anidadas está en el expert, no esparcido por todo el proyecto. Además resuelve el `null`, que es
justamente el caso que rompe los mappers locales. `LogroService` demuestra por qué importa:
tiene 3 mappers distintos (`toResponse`, `toRecompensaResponse`, `toUsuarioLogroResponse`)
porque cada vista del grafo JPA exige un mapeo distinto.

**`CampeonatoAccesoService`** — 17 métodos públicos, todos respondiendo la misma pregunta
*qué puede hacer este usuario sobre este campeonato*. Concentrar la decisión tiene su propio
Javadoc: *"Unico lugar donde se decide que puede ver y que puede tocar cada usuario sobre un
campeonato. No se puede resolver con `@PreAuthorize` porque 'es dueno de este campeonato'
depende del recurso, no del token."*

**Cumplimiento parcial, y conviene decirlo.** Las entidades JPA del proyecto son **anémicas**:
no tienen comportamiento de dominio, solo campos, getters y setters de Lombok, más el
callback de persistencia `Carrera.prePersist()`. El expert de datos está en los servicios,
no en las entidades. Es el compromiso conocido de JPA + capas, no una buena aplicación de
Information Expert.

### 2.3 Controller

> *Una clase que recibe eventos del sistema externo y coordina el trabajo.*

En este backend lo aporta el framework: **31 clases anotadas `@RestController`**. Cada una
recibe el evento HTTP, traduce a tipos del dominio y coordina, delegando el trabajo real. La
regla del proyecto es que el controller **nunca toca un repository** — su trabajo es
traducir y delegar.

Ejemplo del ciclo completo de un Controller GRASP:

```
ResultadoCarreraController.listarPorCarrera()   # evento externo (HTTP GET)
  └─► ResultadoCarreraService.listarPorCarrera(carreraId, visor)
        └─► CampeonatoAccesoService.exigirVeCarrera(visor, carrera)   # decisión
              └─► ResultadoCarreraRepository.find...                # datos
```

El punto donde se ve por qué importa: el **check de autorización tiene que vivir dentro de la
transacción**, o se produce `LazyInitializationException`. Ese fue el bug corregido en
`0ce1eb9`, que movió los checks desde el controller a los servicios. Un Controller que
coordina sin contexto transaccional se rompe solo.

El segundo Controller del proyecto es la cadena de filtros de Spring Security
(`SecurityConfig`): recibe cada request, decide, y `continue` o corta.

### 2.4 High Cohesion

> *Agrupa lo que cambia junto.*

Cada servicio tiene **una sola razón para cambiar**:

| Clase | Responsabilidad única |
|---|---|
| `CampeonatoAccesoService` | Decidir acceso a un campeonato |
| `EntityMapper` | Resolver entidades anidadas en DTOs planos |
| `VueltaService` | Lectura y análisis de telemetría de vueltas |
| `SistemaPuntosFactory` | Resolver la estrategia de puntaje |

`CampeonatoAccesoService` es el ejemplo canónico: 17 métodos, 3 repositorios, y **cero
métodos que muten datos**. Todo lo que hace es decidir. Por eso agregar una regla de
visibilidad (por ejemplo, "el admin ve su campeonato aun si es privado") es un cambio
contenido en un archivo.

**Advertencia de cohesión en curso.** `VueltaService` concentró con el tiempo el listado, el
análisis por vuelta y el resumen estadístico. Hoy las tres siguen siendo sobre vueltas, pero
el resumen ya cruza con clasificación y resultados. Si sigue creciendo, ahí aparece una fuga
de cohesión.

### 2.5 Low Coupling

> *Reduce el acoplamiento entre clases.*

Cuatro mecanismos, en orden de fuerza:

**Depender de interfaces.** `SistemaPuntos` es lo que se inyecta en `CampeonatoService`; los
repositories son interfaces Spring Data; `IdempotenciaSesionService` no se referencia desde
el controller. Se puede cambiar la implementación sin tocar al cliente.

**Records DTO como frontera.** Las entidades JPA nunca escapan de la capa de servicio. El
controller y el JSON solo ven `record`s inmutables. Esto no es solo encapsulamiento:
serializar una entidad con `@ManyToOne(LAZY)` fuera de sesión lanza
`LazyInitializationException`, así que el DTO es la defensa.

**Inyección por constructor con campos `final`.** Lombok `@RequiredArgsConstructor` en los
37 servicios. Cero estado mutable, cero setters, y el grafo de dependencias queda explícito
en la firma.

**Absorber diferencias de forma en el borde.** En el frontend, `ApiService.normalizeList()`
tolera que un endpoint devuelva un array plano y otro una página `{content}`. El
componente que llama no necesita saber cuál era cuál.

### 2.6 Polymorphism

> *Permite que una variable referencia objetos de subclases con el mismo uso y que el
> software se comporte correctamente.*

Los tres puntos de GoF del proyecto, y ningún `switch` ni `instanceof` que los reemplace:

| Jerarquía | Cantidad | Dispatch |
|---|---|---|
| `implements SistemaPuntos` | **2** | por interfaz |
| `extends ValidadorInscripcion` | **3** | por clase abstracta |
| `extends ImportadorSesion` | **2** | por clase abstracta |

Medido hoy: la búsqueda de `implements` en todo `service/` da **2 resultados, ambos de
puntos**. Y `instanceof` da **0 ocurrencias** en todo `src/main/java`. El polimorfismo está
donde corresponde y en ningún otro lado.

En el frontend el polimorfismo llega por inyección de dependencias: los componentes
obtienen `ApiService` con `inject(ApiService)` (`race-detail.component.ts:51`) y dependen del
token, no de una implementación concreta. Cambiar la capa HTTP no obliga a tocar ningún
componente.

### 2.7 Pure Accumulator

> *Acumula resultados en un contenedor o variable a medida que procesa cada elemento de un
> origen, en vez de guardar resultados intermedios.*

Dos sitios, y en los dos el acumulador reemplaza trabajo repetido.

**`VueltaService.analisisCarrera()` — el caso que lo justifica.** Para medir el atraso contra
el líder hay que conocer la suma acumulada de vueltas de **todos** los pilotos, no solo la del
piloto analizado. En vez de recalcular esa suma en cada vuelta y en cada piloto:

```java
Map<Long, Map<Integer, Long>> cumulative = new HashMap<>();
for (VueltaCarrera v : raceLaps) {
    Long uid = v.getUsuario().getId();
    int lap = v.getNumeroVuelta();
    long cumPrev = cumulative.getOrDefault(uid, Map.of()).getOrDefault(lap - 1, 0L);
    cumulative.computeIfAbsent(uid, k -> new HashMap<>()).put(lap, cumPrev + v.getTiempoMs());
}
```

El loop ya viene ordenado por `Usuario_Id` y `NumeroVuelta`, así que el acumulado avanza
correcto con una sola pasada. Después el mapa se **consulta** para calcular `minCum` y la
posición por vuelta, sin volver a sumar nada. Sin el acumulador, cada una de las N vueltas
recalcularía los tiempos acumulados de los M pilotos: O(N·M).

**`VueltaImportService.importar()` — numerar sin consultar el máximo.** En vez de buscar el
número de vuelta más alto y sumarle uno (que exige una query por cada piloto), el contador
vive en memoria durante la importación:

```java
int numero = numeros.merge(lap.driverGuid(), 1, Integer::sum);
```

Cero queries, y además no depende del estado previo de la base: si la vuelta 1 y la 2 llegan
en cualquier orden, el merge las numera igual.

### 2.8 Pure Function

> *Una función que no produce efectos secundarios: la misma entrada da siempre la misma
> salida y no altera el estado global.*

**`SistemaPuntosFactory.normalizar(clave)`** es la más limpia del proyecto: `trim()` +
`toLowerCase(Locale.ROOT)`, `null` → `""`. No toca nada, y como efecto práctico hace que
`"top 10"`, `"TOP 10"` y `"  Top 10 "` resuelvan al mismo bean. Como es pura, es trivial de
probar: los tests del factory la ejercitan sin levantar Spring.

**Los helpers de `EntityMapper`** (`resolveCategoriaNombre`, `resolveCarreraInfo`,
`resolveUsuarioBasico`) son funciones puras estáticas, con el `null` resuelto como
requirement. Se reusan en 5 servicios en lugar de reescribir el null-check.

**`VueltaService.minSector(vueltas, sector)`** es un selector puro: recibe la lista y el número
de sector, devuelve el mínimo o `null`, sin efectos.

La ventaja práctica: las funciones puras son las que se pueden testear en aislamiento. El
factory de puntos se instancia en los tests con
`List.of(new SistemaPuntosF1(), new SistemaPuntosTop10())`, sin contenedor.

### 2.9 Protected Variations

> *Identifica los puntos donde se anticipa un cambio y encapsúlalo para que el cambio no
> afecte a los demás.*

Cuatro variaciones están protegidas de verdad:

| Cambio anticipado | Qué lo aísla |
|---|---|
| Nuevo sistema de puntaje | `SistemaPuntosFactory` + autodescubrimiento por contenedor |
| Nueva regla de inscripción | La cadena `ValidadorInscripcion` con `@Order` |
| Cambiar la persistencia | Interfaces `Repository` |
| Concerns transversales (caché, transacciones) | Proxies de Spring |

El test de fuego es concreto: agregar un esquema de puntos es **un `@Component` nuevo**, y
agregar una regla de inscripción es **un validador nuevo con su `@Order`**. En ninguno de los
dos casos se edita el cliente del patrón.

**Donde se rompe: el fan-out de notificaciones.** Los sitios internos que generan
notificaciones **no usan el servicio de notificaciones**; escriben el repositorio
directamente. Medido hoy: **7 llamadas a `notificacionRepository.save` fuera de
`NotificacionService`**, repartidas en 5 servicios:

| Servicio | Llamadas |
|---|---|
| `LogroService` | 2 (`notificarLogro`, `notificarRecompensa`) |
| `CarreraService` | 2 (cambios de estado de carrera) |
| `SancionService` | 1 (sanción aplicada) |
| `ApelacionService` | 1 (apelación resuelta) |
| `CampeonatoMiembroService` | 1 (piloto agregado) |

No se puede reutilizar `NotificacionService.create(...)` internamente porque su firma toma el
DTO de API, no una entidad de dominio. El coste: agregar un tipo de notificación obliga a
**editar 7 lugares**, y es fácil que uno se quede atrás. Aquí la variación no está protegida,
porque el criterio de bug lo define el requerimiento, no el patrón.

---

## 3. GoF

### 3.1 Simple Factory — creacional

GoF canónico en `service/puntos/`. Es la fábrica de objeto único central del proyecto:

```
SistemaPuntos (interfaz)
├── SistemaPuntosF1      tabla List.of(25, 18, 15, 12, 10, 8, 6, 4, 2, 1)
└── SistemaPuntosTop10   tabla List.of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
```

| Pieza | Ubicación |
|---|---|
| Contrato | `SistemaPuntos.java` — `clave()`, `nombre()`, `puntosPara(int)` |
| Implementaciones | `SistemaPuntosF1.java`, `SistemaPuntosTop10.java` |
| Fábrica | `SistemaPuntosFactory.de(String)` |
| Catálogo para la UI | `SistemaPuntosFactory.disponibles()` → `SistemaPuntosCatalogo` record |
| Endpoint | `SistemaPuntosController` — `GET /api/sistemas-puntos` |

**GoF estricto**: el factory decide qué clase concreta crear a partir de un parámetro, y no
expone la instanciación al cliente. La diferencia con el resto del proyecto es que acá la
fábrica recibe `List<SistemaPuntos>` por inyección en vez de hacer `new`: es el mismo patrón,
resuelto con el contenedor en lugar de un `switch`.

Existe un **segundo dispatch por clave** en `SesionServidorService.importar()`, que busca el
`ImportadorSesion` cuyo `tipo()` coincide con el del JSON. Misma forma, política opuesta y
correcta: el factory de puntos **cae al default** ante clave desconocida, mientras que el
importador **lanza `BusinessException`** al operador, porque un tipo de sesión no soportado
debe ser visible.

Los tests documentan el patrón: `de(null)`, `de("")` y `de("F1_SPRINT")` caen al default; si
desaparece la estrategia default, el test falla; y `SistemaPuntosFactoryTest` congela las
claves canónicas que el frontend persiste.

### 3.2 Singleton — creacional

**La intención del patrón se cumple; el mecanismo es otro.** Los 37 servicios son
singletons del contenedor de Spring por defecto de `@Service` +
`@RequiredArgsConstructor` + campos `private final`.

Medido hoy: **0 ocurrencias de `getInstance()`** en `src/main/java`. Es decir, no hay ni una
sola implementación del patrón clásico a la GoF.

Es correcto que sea así: el patrón clásico con constructor privado existe para controlar la
instanciación desde el propio código. Con inyección de dependencias, el contenedor ya garantiza
la unicidad, y además da ciclo de vida, proxies y testabilidad con mocks. Instanciar a mano un
servicio con 10 collaborators sería una regresión.

Dos clases utilitarias usan constructor privado
(`EntityMapper`, `FileUtil`). **No son Singleton**: son Utility Class, que no tienen instancia
alguna. `FileUtil.obtenerExtension` se reusa en 3 servicios.

### 3.3 Strategy — conductual

> *Define una familia de algoritmos encapsulados e intercambiables. El cliente recibe el
> algoritmo como parámetro y no depende de la clase concreta.*

GoF puro en `service/puntos/`, con la tabla de puntos detrás de una interfaz:

```java
public interface SistemaPuntos {
    String clave();
    String nombre();
    int puntosPara(int posicion);
}
```

Es el **único Strategy del backend implementado con clases** (los `implements` en todo
`service/` dan 2 resultados, ambos de puntos). El cliente inyecta el factory, llama `de(clave)`
y usa `puntosPara(...)`; nunca menciona `SistemaPuntosF1`. Esa es la Hint del patrón.

**Strategy Total**: ambas implementaciones devuelven `0` fuera de rango en vez de lanzar
excepción, así que el cliente no necesita guarda de rango.

**Variante**: en `SecurityConfig`, el par `authenticationEntryPoint` y `accessDeniedHandler`
son Strategy con lambdas — comportamiento intercambiable pasado en el punto de uso, sin
subclase nombrada. Cuenta como Strategy aunque no sea una clase.

### 3.4 Chain of Responsibility — conductual

Variante de pipeline en `service/inscripcion/`:

| Eslabón | Clase | `@Order` | Regla |
|---|---|---|---|
| 1 | `InscripcionesAbiertasValidador` | 1 | Estado `PROGRAMADA`/`INSCRIPCIONES_ABIERTAS` + ventana de cierre de 5 min |
| 2 | `PertenenciaValidador` | 2 | `accesoService.puedeParticiparEnCarrera(...)` |
| 3 | `RequisitosEloValidador` | 3 | Elo en el rango de la categoría; **skip** si el campeonato es `PRIVADO` |

El esqueleto:

```java
public abstract class ValidadorInscripcion {
    private ValidadorInscripcion siguiente;

    public final void validar(Carrera carrera, Usuario usuario) {
        validarPropio(carrera, usuario);
        if (siguiente != null) {
            siguiente.validar(carrera, usuario);
        }
    }

    protected abstract void validarPropio(Carrera carrera, Usuario usuario);
}
```

Cada subclase implementa solo `validarPropio`. El `final` en `validar()` es lo que garantiza
que nadie rompa el avance de la cadena.

**Variante respecto del GoF canónico**: el CoF clásico suele propagar el control de salida para
cortocircuitar. Acá **no hay cortocircuito**: los tres eslabones se ejecutan siempre, en orden,
y el rechazo es `throw new BusinessException(...)`. Es un pipeline de validación con fail-fast
por excepción.

**El orden es semántico.** Los `@Order` no son decorativos: definen **qué error ve el piloto
primero**. Hoy un piloto sin Elo suficiente en un campeonato privado recibe el mensaje de
pertenencia (eslabón 2) y nunca llega al de Elo (eslabón 3). `InscripcionValidacionTest` lo
verifica con `verifyNoInteractions(accesoService)`, probando que el eslabón 1 frena **antes**
de llegar al 2.

> **Riesgo verificado — estado compartido escrito concurrentemente.** `InscripcionValidacion`
> reconstruye la cadena en cada request (`encadenar()`), escribiendo el campo mutable
> `siguiente` desde beans singleton. Hoy es idempotente: siempre reconstruye la misma cadena
> por el mismo `@Order`. Pero es estado compartido escrito concurrentemente sin sincronizar. El
> campo podría ser `volatile`, o la cadena construirse una sola vez.

### 3.5 Template Method — conductual

GoF puro en `service/sesion/`:

```
ImportadorSesion (abstract)          ImportadorRace      ImportadorQualify
├── final importar()                       └── super("RACE")
│     ├── validar()    ← hook (cuerpo vacío)
│     ├── procesar()   ← abstract
│     └── return tipo
└── validar()        ← hook
```

El dato que distingue a cada subclase (`"RACE"` / `"QUALIFY"`) se empuja por constructor;
`ImportadorRace` y `ImportadorQualify` implementan solo `procesar()`. El esqueleto está en la
base, el algoritmo no se duplica.

> **Punto de extensión muerto**: `ImportadorSesion.validar()` **no lo overridea ninguna clase
> de producción**. El único resultado de `protected void validar(` en todo `src/main/java` es
> la declaración misma; solo lo ejercitan los tests. El paso `validar` del `importar()` no hace
> nada en el flujo real. No es bug —el default vacío lo hace inofensivo— pero es un gancho que
> promete una capacidad que no existe.

### 3.6 Proxy — estructural

El patrón más presente del proyecto y el único aplicado en masa por framework: **14
`@Cacheable`, 27 `@CacheEvict` y 168 `@Transactional`** en `service/`.

El Spring AOP genera un proxy alrededor del bean. La interposición **es** el patrón, y no es
decorativa:

- La caché se consulta **antes** de ejecutar el cuerpo del método. Por eso el gate de
  membresía no puede vivir dentro del método cacheado: si viviera, un usuario sin permiso
  leería el resultado cacheado de otro. La razón está escrita en el propio código, en
  el comentario de `CampeonatoService.listarTablaPosiciones` y en `CarreraService`.
- Por la misma razón los checks de autorización se movieron **dentro** de los servicios
  transaccionales (commit `0ce1eb9`): fuera del proxy no hay transacción, y sin transacción el
  grafo JPA lazy revienta.

**Consecuencia estructural**: `this.metodo()` dentro de la misma clase **no** pasa por el proxy,
así que `@Cacheable` y `@Transactional` se ignoran en llamada interna. Verificado hoy: la
búsqueda de `this.\w+\(` en `service/` da **1 resultado**, y es una línea de comentario en el
Javadoc de `SancionService` que explica precisamente el problema. No hay auto-invocación real.

> **Bug ya corregido (`7034242`) — la caché `usuarios` quedaba sin evacuar**. La causa era
> Proxy puro: un `@CacheEvict` cubría solo `delete`, mientras `updatePerfil`, `updateRating`,
> `ResultadoCarreraService.cargarResultados` y `SancionService` escribían campos cacheados sin
> evacuar. El alcance real era más ancho de lo sospechado: `cargarResultados` —la operación más
> frecuente del sistema— dejaba el ranking público entero desactualizado.

### 3.7 Null Object — estructural

> *Provee un objeto sustituto con valores por defecto en lugar de `null`, para que el cliente
> pueda llamar sin comprobar la ausencia.*

GoF aplicado a colecciones, en `VueltaService.analisisCarrera()` y `resumenCarrera()`:

```java
cumulative.getOrDefault(uid, Map.of()).getOrDefault(lap, 0L)
porVuelta.getOrDefault(lap, List.of())
```

`Map.of()` y `List.of()` son **inmutables y vacíos**. Absorber sobre ellos llamadas de
lectura es seguro: `get` devuelve `null`, no hay `NullPointerException`. Es Null Object de
verdad, con la salvedad de que el objeto nulo es de la biblioteca (`ImmutableCollections`) en
vez de una clase propia del proyecto — el mismo criterio que usa Java para
`Collections.emptyList()`.

El beneficio concreto se ve en el anidamiento: `getOrDefault(uid, Map.of()).getOrDefault(lap,
0L)` evita el doble `if (mapa != null)` que haría falta sin él. En el mismo método,
`Long.MAX_VALUE` se usa como centinela para "sin dato", que es el otro extremo del mismo
problema: reemplazar la ausencia por un valor que no estalla al comparar.

---

## 4. Ausencias verificadas

Se buscó cada patrón de GRASP y GoF en `src/main/java`. **Todos estos dan cero resultados**:

| Patrón | Resultado |
|---|---|
| **Adapter** propio (una clase que envuelve a otra del proyecto) | no existe — el rol lo juega Spring (`PasswordEncoder` ← `BCryptPasswordEncoder`) |
| **Bridge / Mediator / Memento / Interpreter** | no existen |
| **Composite** | no existe — la cadena de inscripción es lineal, no un árbol |
| **Decorator** propio | no existe |
| **Factory Method** | no existe como método que decide la subclase — la decisión está en la Simple Factory |
| **Iterator** propio | no existe — toda iteración es `for`/`stream` |
| **Observer** / eventos (`ApplicationEventPublisher`, `@EventListener`) | **no existe**, y es la ausencia con más consecuencias: ver §2.9 |
| **State** (máquina de estados explícita) | no existe — los estados son enums **datos** (`EstadoCarrera`, `EstadoIncidente`, `EstadoCampeonato`, `EstadoApelacion`, `EstadoInscripcion`: constantes sin comportamiento) y las transiciones son `if` y asignaciones |
| **Visitor** | no existe — ni de objetos ni de enum; el dispatch por `switch` de `LogroService` y `SancionService` es la variante "poor man's Strategy", no un Visitor |
| **Flyweight** | no existe |
| **Singleton a la GoF** (`getInstance()`) | no existe — 0 ocurrencias |
| **Singleton Null Object propio** | no existe el patrón completo — hay Null Object de colección (§3.7) y get-or-create |
| **MapStruct** u otro mapper declarativo | no existe — el mapeo es a mano |

Las dos ausencias que pesan:

**No hay Observer.** `NotificacionService` solo lo usan los controllers; los 7 sitios internos
escriben el repositorio directamente (§2.9). El idioma natural sería Observer con
`ApplicationEventPublisher`, o un notificador interno con firma de dominio. Ninguno existe.

**No hay State.** Los 5 enums de estado son cajas de constantes, no objetos. La máquina de
estados de un campeonato o de un incidente está implícita en `if`s repartidos entre servicios.
Consecuencia práctica: ninguna transición puede listarse ni validarse en un solo lugar.

---

## 5. Referencias

| Documento | Contenido |
|---|---|
| `docs/arquitectura-y-patrones.md` | Arquitectura, capas, reglas de trabajo y deuda técnica |
| `docs/flujos.md` | Flujos funcionales y modelo de datos |
| `docs/formulas-rating.md` | Fórmulas de Elo, Safety Rating y puntos |
| `docs/analisis-mejoras.md` | Análisis previo de mejoras |