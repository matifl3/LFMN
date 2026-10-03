# Cómo se construyó LFM Nacional

Documento técnico del proyecto: qué hay, cómo está armado y **qué patrones se eligieron y por qué**.
Sirve para onboarding y para no repetir los errores que ya encontramos.

- **Backend**: `lfmNacional/` — Spring Boot 4.1.0, Java 17, Maven
- **Frontend**: `frontend/` — Angular 22
- **Infraestructura**: frontend en Vercel, API en Render, BD en Aiven (MySQL)

---

## 1. Estructura general

Monorepo con **dos apps independientes**. No hay monorepo tool (nx, turbo, lerna):
simplemente dos carpetas que se despliegan por separado.

```
LFMN/
├── lfmNacional/          API REST — Spring Boot
│   ├── src/main/java/org/example/lfmnacional/
│   │   ├── controller/    31 clases — HTTP, una por recurso
│   │   ├── service/       52 clases — lógica de negocio
│   │   ├── repository/    29 interfaces — Spring Data JPA
│   │   ├── entity/        29 entidades JPA
│   │   ├── dto/           80 records — contrato de la API
│   │   ├── enums/         15 enums
│   │   ├── security/      JWT + rate limit
│   │   ├── config/        Security, cache, schedulers, DataSeeder
│   │   ├── mapper/        EntityMapper
│   │   ├── exception/     jerarquía de errores + handler global
│   │   └── service/inscripcion/  cadena de validadores
│   └── src/test/java/     17 clases — 136 tests
├── frontend/             SPA — Angular 22
│   └── src/app/
│       ├── core/          servicios singletons, guards, interceptores, modelos
│       ├── features/      12 features, cada una con sus componentes
│       └── app.routes.ts  rutas lazy
├── scripts/              solo backup-mysql.sh
├── docs/                 este documento y otros
└── .github/workflows/    ci.yml (solo CI)
```

**Volumen**: 250 clases Java de producción, 136 tests, 15 componentes Angular.

---

## 2. Backend: los patrones que usamos

### 2.1 Capas estrictas: `controller → service → repository`

El controller **no habla nunca con el repository**. Su trabajo es:

1. traducir HTTP (path, query params, body)
2. delegar al service
3. envolver el resultado

```java
@GetMapping("/carrera/{carreraId}")
public List<ResultadoCarreraResponse> listarPorCarrera(
        @PathVariable Long carreraId,
        @AuthenticationPrincipal Usuario usuario) {
    return resultadoCarreraService.listarPorCarrera(carreraId, usuario);
}
```

El service concentra la lógica. El repository solo habla con la base.

> **Por qué importa:** los dos bugs más caros del proyecto aparecieron en esta frontera. Ver §3.

### 2.2 DTOs como `record` inmutables

**80 records**, cero clases DTO mutables. El JSON sale del record, no de la entidad.

```java
public record ResultadoCarreraResponse(
        Long id, Long usuarioId, String nombrePiloto,
        Integer posicionFinal, Long tiempoTotal) {}
```

Consecuencia práctica: los nombres de campo en camelCase **llegan 1:1 al JSON** y se reflejan
literalmente en `frontend/src/app/core/models/models.ts`. Si tocás un DTO, actualizás la interface TS.

Los `record` además te dan `equals`, `hashCode` y `toString` gratis, y son serializables por Jackson
sin anotaciones.

### 2.3 `EntityMapper`: componentes anidados

Los DTO anidados (dentro de un resultado está el usuario, que tiene nombre y foto) no se resuelven
con `@Embedded` ni con cascada. Hay un mapper estático con **records de apoyo**:

```java
public record UserInfo(Long id, String nombrePiloto, String fotoPerfil) {}

public static UserInfo resolveUsuarioBasico(Usuario usuario) {
    if (usuario == null) return null;          // null-safe
    return new UserInfo(usuario.getId(), usuario.getNombrePiloto(), usuario.getFotoPerfil());
}
```

Cada método devuelve un recordchico o `null`, y el mapper principal los compone. Todas las
verificaciones de null están **en un solo archivo**.

### 2.4 Un único lugar para la autorización: `CampeonatoAccesoService`

Este es el patrón más importante del backend. Los permisos **no** se resuelven con `@PreAuthorize`
porque "es dueño de *este* campeonato" depende del recurso, no del token.

```java
@Service
@RequiredArgsConstructor
public class CampeonatoAccesoService {
    public boolean administra(Usuario u, Campeonato c) { ... }
    public boolean veContenido(Usuario u, Campeonato c) { ... }
    public boolean participoEnCarrera(Usuario u, Carrera c) { ... }

    // Los que lanzan, para usar en controllers y servicios
    public void exigirAdministra(Usuario u, Campeonato c) { ... }
    public void exigirVeCarrera(Usuario u, Carrera c) { ... }
}
```

La jerarquía de roles:

| Rol | Puede |
|---|---|
| `ADMIN` | todo; es el único que puede crear campeonatos |
| `ADMIN_CAMPEONATO` | administra los campeonatos privados que le asignen, pero no crea ninguno |
| `COMISARIO` | lectura de todo por su labor, **no compite** |
| `USUARIO` | ve público y lo suyo |

El detalle fino está en la separación entre **ver** y **competir**:

```java
// El comisario tiene lectura de los privados por su labor, pero no compite:
// si se dejara pasar por veCarrera terminaria inscripto en un campeonato
// que no organiza.
public boolean puedeParticipar(Usuario u, Campeonato c) {
    return administra(u, c) || esMiembro(u, c);
}
```

Y la visibilidad distingue **el campeonato** de **su contenido**. El nombre, temporada y categoría
son públicos siempre; el roster, la tabla, las carreras y los resultados exigen membresía:

```java
public boolean veContenido(Usuario u, Campeonato c) {
    if (c.getVisibilidad() == PUBLICO) return true;
    if (administra(u, c) || esComisario(u)) return true;
    return esMiembro(u, c);
}
```

### 2.5 La regla de oro: **toda relación se toca dentro de una transacción**

Este patrón nos costó un bug real (§3.1), así que quedó escrito como norma:

> `Carrera.campeonato` es `FetchType.LAZY` y el proyecto tiene `open-in-view=false`.
> Por lo tanto **cualquier código que lea una relación debe ejecutarse dentro de `@Transactional`**.

`CarreraService.getEntity()` **no** es transaccional y devuelve una entidad despegada:

```java
public Carrera getEntity(Long id) {          // sin @Transactional: devuelve detached
    return carreraRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("..."));
}
```

Por eso el check de acceso vive en el **servicio** (transaccional) y no en el controller:

```java
// MAL: el controller llama a getEntity() y el check ocurre sin sesión
accesoService.exigirVeCarrera(usuario, carreraService.getEntity(carreraId));   // LazyInit → 500

// BIEN: el check ocurre dentro del @Transactional del servicio
@Transactional(readOnly = true)
public List<ResultadoCarreraResponse> listarPorCarrera(Long carreraId, Usuario visor) {
    championshipAcceso.exigirVeCarrera(visor, carreraService.getEntity(carreraId));
    return resultadoCarreraRepository.findByCarrera_IdOrderByPosicionFinalAsc(carreraId)
            .stream().map(this::toResponse).toList();
}
```

Cuando un método del service llama a otro del **mismo** service (como `resumenCarrera` →
`analisisCarrera`), la anotación `@Transactional` **no** se reaplica: el proxy de Spring solo intercepta
llamadas externas. No pasa nada porque ya estamos dentro de la transacción.

### 2.6 Errores: `ApiError` y un handler global

Jerarquía corta, mapeada en `GlobalExceptionHandler`:

| Excepción | HTTP | Cuándo |
|---|---|---|
| `BusinessException` | 400 | regla de negocio (privado, ELO, estado) |
| `ResourceNotFoundException` | 404 | no existe |
| `AccessDeniedException` | 403 | rol insuficiente |
| `DataIntegrityViolationException` | 409 | FK o unique violada |
| `Exception` | 500 | todo lo demás, mensaje genérico |

El mensaje de negocio **viaja al front** en `ApiError.message`, así que el frontend puede mostrarlo.
El 500 nunca expone la excepción interna.

### 2.7 Seguridad: JWT stateless

```java
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

- `JwtAuthenticationFilter` (jjwt 0.12.6) lee el token y arma el `Authentication` con la entidad `Usuario`
- El resto de la app usa `@AuthenticationPrincipal Usuario usuario` — **la entidad, no un string**
- `RateLimitFilter` para abuseo de login
- CSRF deshabilitado (no hay cookies ni sesión)

Lo bueno de meter la entidad `Usuario` en el `principal`: los services reciben un objeto y pueden
consultar `getRol()` directamente, sin volver a la base.

El `SecurityConfig` es una lista explícita de rutas públicas. Ojo con el orden: Spring evalúa de arriba
abajo y **`/api/campeonatos/{id}` arrastraba a `/mios` y `/mis-membresias`**, así que esas están
declaradas **antes** y como `authenticated()`.

### 2.8 Idempotencia por hash del contenido

La ingesta de sesiones de Assetto Corsa se hace por import manual
(`POST /api/sesiones/importar?carreraId=N`), **no hay folder watcher** a propósito: en Render el disco
es efímero y la app no debe depender de él.

Cada JSON se hashea con **SHA-256** y se guarda en `sesion_procesada.clave`, que es única:

- mismo JSON → `409 Conflict`, no reprocesa
- los incidentes autogenerados deduplican por `incidente.clave_origen`, así que un reintento no duplica colisiones

### 2.9 Caché con eviction explícito

`spring-boot-starter-cache` con `ConcurrentMapCacheManager`. Se usa para lo que se lee mucho y cambia
poco: `anuncios`, `archivos_carrera`, `tabla_posiciones`, `categorias`, `usuarios`.

Dos cosas aprendidas:

**El gate de membresía NO puede vivir adentro de un `@Cacheable`.** El cache es por clave, no por
usuario: si cacheás el resultado de una tabla de posiciones filtrada, el primero que lo pide lo
contamina para todos. Por eso se cachea el cálculo completo y el filtro va **en la query**.

**Si un método escribe, hay que evictar la caché que lo lee:**

```java
@Transactional
@CacheEvict(value = "usuarios", allEntries = true)   // sin esto, el ranking queda viejo
public List<ResultadoCarreraResponse> cargarResultados(...) {
    ...
    recalcularEloYSafetyRating(carrera, resultados);  // escribe elo y safetyRating
}
```

### 2.10 Cadena de validadores en la inscripción

En vez de un `if` gigante, cada regla es un bean que implementa la misma interfaz y se ordena con
`@Order`:

```java
@Component
@Order(3)
public class RequisitosEloValidador extends ValidadorInscripcion {
    @Override
    protected void validarPropio(Carrera carrera, Usuario usuario) {
        if (carrera.getCampeonato().getVisibilidad() == PRIVADO) {
            return;   // en privado no se exige ELO
        }
        ...
    }
}
```

Para agregar una regla nueva: una clase, un `@Order`. No se toca el servicio.

### 2.11 Migraciones: Flyway en espera, `ddl-auto` activo

Hay una carpeta `db/migration` con `V1__` y `V2__`, y un `V3__admin_campeonato.sql` escrito para este
problema. **Pero Flyway está deshabilitado** y dev y prod usan `ddl-auto=update`.

Consecuencia real, y la lesson más cara del proyecto: **`ddl-auto=update` no puede expandir un ENUM de
MySQL**. Agregar un valor a un enum Java no cambia la columna, y el `DataSeeder` revienta al arrancar
con `Data truncated for column 'rol'`.

```sql
-- Hay que correrlo a mano, una vez, contra la BD de prod
ALTER TABLE usuario
  MODIFY COLUMN rol enum('ADMIN','ADMIN_CAMPEONATO','COMISARIO','USUARIO')
  COLLATE utf8mb4_unicode_ci NOT NULL;
```

Después de arreglarlo revisamos **los 15 ENUM** contra los enums Java para no encontrar otro caso igual.

### 2.12 Tests: H2 en memoria

`./mvnw clean verify` corre **136 tests sin tocar MySQL**. En `src/test/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false      # igual que prod: reproduce el bug de lazy loading
spring.flyway.enabled=false
```

Dos estilos conviven:

- **Unitarios con Mockito** (`@ExtendWith(MockitoExtension.class)`) para lógica aislada. Rápidos.
- **De integración con `@SpringBootTest`** para lo que necesita comportamiento real de JPA.

Un test con mocks **nunca detecta un `LazyInitializationException`** (los proxies lazy no existen ahí).
Por eso hay que probar contra H2. Los tests de integración no llevan `@Transactional` a propósito:
reproducen el mismo límite transaccional que tiene un controller.

Restricción práctica: `email` y `nombrePiloto` son únicos y el contexto se comparte entre tests, así que
los datos se generan con un sufijo `UUID` en `@BeforeEach`.

---

## 3. Los dos bugs que después costaron tiempo

Documentados porque ilustran por qué los patrones de §2.4 y §2.5 existen.

### 3.1 Pestañas vacías por `LazyInitializationException`

**Síntoma**: en la pantalla de carrera, las pestañas Resultado, Clasificación y Análisis salían vacías.

**Diagnóstico**: el frontend traga los errores con `catchError(() => of([]))`, así que cualquier 500
se ve como "vacío" sin mensaje. El backend devolvía 500, no 400.

**Causa**: exactamente el patrón de §2.5. El controller llamaba `exigirVeCarrera` sobre una `Carrera`
despegada, y `veCarrera` toca `carrera.getCampeonato()` fuera de sesión.

Lo que despistaba: `GET /api/carreras/4` devolvía 400 bien, porque `CarreraService.getById` **sí** es
transaccional. Solo fallaban los endpoints con el check en el controller.

**Fix**: mover el check a los servicios transaccionales y borrar las dependencias de acceso de los
controllers. Cubierto por `CarreraPrivadaLazyLoadingTest`.

### 3.2 El piloto que no veía su propia carrera

**Síntoma**: un piloto que se inscribió y corrió no veía esa carrera, con "pertenece a un campeonato
privado del que no sos miembro".

**Causa**: el filtro de visibilidad solo contemplaba **membresía** y **ownership**, nunca
**participación**. El filtro de SQL era:

```sql
where r.usuario.id = :usuarioId
  and (:spectatorId is null                    -- admin global o comisario
       or r.carrera.campeonato.visibilidad <> 'PRIVADO'
       or r.carrera.campeonato.admin.id = :spectatorId
       or exists (select m from CampeonatoMiembro m ...))   -- solo membresía
```

Un piloto con resultado cargado y sin fila en `campeonato_miembro` quedaba fuera.

**Fix**: participación habilita a ver **esa carrera**, no el campeonato entero:

```java
public boolean veCarrera(Usuario usuario, Carrera carrera) {
    if (veContenido(usuario, carrera.getCampeonato())) return true;
    return participoEnCarrera(usuario, carrera);   // se inscribió o corrió
}
```

Y en las queries de historial se agregó `or r.usuario.id = :spectatorId` — **también en el
`countQuery`**, o la paginación miente.

De paso, `CampeonatoService.create()` aceptaba `PRIVADO` con `adminId` nulo, dejandounal campeonato
que nadie podía administrar ni abrir a público (`cambiarVisibilidad` ya lo rechazaba). Ahora valida.

---

## 4. Frontend: los patrones que usamos

### 4.1 Standalone + signals, sin `NgModule`

Angular 22. Cada componente es `standalone`, y el estado es `signal`, no `BehaviorSubject`:

```typescript
export class RaceDetail {
    readonly resultados = signal<ResultadoCarrera[]>([]);
    readonly resultadosSorted = computed(() =>
        [...this.resultados()].sort((a, b) => (a.posicionFinal ?? 999) - (b.posicionFinal ?? 999)));
}
```

- `signal()` para estado mutable
- `computed()` para derivados — se recalcula solo, no hay `combineLatest` a mano
- `inject()` en vez de constructor con `@Inject`

Cada feature es una carpeta: `features/<nombre>/<nombre>.component.ts|.html|.scss`, y el HTML usa el
nuevo bloque de control `@if` / `@for` en lugar de `*ngIf` / `*ngFor`.

### 4.2 Rutas lazy con guards

Todas las features se cargan bajo demanda:

```typescript
{
    path: 'carreras/:id',
    loadComponent: () => import('./features/races/race-detail/race-detail.component')
        .then((m) => m.RaceDetailComponent),
},
```

Los guards (`authGuard`, `adminGuard`, `organizadorGuard`) son funciones. Ojo: son **UX**, no
seguridad. La seguridad real está en el backend.

### 4.3 `ApiService`: un solo lugar que habla con el backend

```typescript
get<T>(path: string, params?: ApiOptions['params']): Observable<T>
list<T>(path: string, params?: ApiOptions['params']): Observable<T[]>
```

`ApiService` inyecta el JWT, maneja errores con **reintento**, y `list()` pasa por `normalizeList`:

```typescript
list<T>(path: string, params?): Observable<T[]> {
    return this.get<unknown>(path, params).pipe(map((r) => normalizeList<T>(r)));
}
```

`normalizeList` existe porque el backend devuelve paginación `{content: [...]}` en algunos endpoints
y array plano en otros. Sin esto, cada componente tiene que verificar el `Array.isArray()`.

### 4.4 El frontend traga errores — el patrón a mejorar

Este es el punto débil que nos costó el diagnóstico de §3.1:

```typescript
resultados: this.api.list<ResultadoCarrera>('/resultados/carrera/' + id)
    .pipe(catchError(() => of([]))),        // un 500 se ve como "no hay resultados"
```

`catchError(() => of([]))` convierte **cualquier** error en una lista vacía. La UI no distingue
"no hay datos" de "el backend está caído" de "no tenés permiso". Solo `carrera` maneja el error
con un signal `bloqueado`.

Vale la pena cambiarlo a algo tipo:

```typescript
.catchError((err) => {
    this.errorPanel.set(err?.error?.message ?? 'No se pudo cargar');
    return of([]);
})
```

### 4.5 Modelos TS que espejan los records

`core/models/models.ts` es la única fuente de las interfaces. Los tipos se crean como unión de los
enums Java:

```typescript
export type VisibilidadCampeonato = 'PUBLICO' | 'PRIVADO';
```

Si tocás un record del backend, actualizás acá en el mismo commit. El build (`npm run build`) es el
typecheck: **no hay script de lint ni de typecheck**.

### 4.6 Formato

`prettier` con `singleQuote`, `printWidth 100`, y parser `angular` para los `.html`.

---

## 5. Build y despliegue

### 5.1 Comandos

```bash
# backend — build + 136 tests, H2, sin MySQL
cd lfmNacional && ./mvnw -B --no-transfer-progress clean verify

# frontend — el build ES el typecheck
cd frontend && npm ci && npm run build
```

Dev: `npm start` en `:4200` con proxy `/api` → `http://localhost:8080` (`proxy.conf.json`).

### 5.2 Topología

```
Vercel     frontend (dist/)      lfmn.vercel.app
Render     API                   lfmn.onrender.com   (Dockerfile, perfil prod)
Aiven      MySQL gestionada      puerto 16861, TLS
```

Cada push a `main` redeploya ambos. `.github/workflows/ci.yml` corre `verify` en cada push y PR.

### 5.3 Variables de entorno

| Variable | Para qué |
|---|---|
| `DB_URL` | JDBC de Aiven, con `ssl-mode=REQUIRED` |
| `DB_USERNAME` / `DB_PASSWORD` | credenciales |
| `JWT_SECRETO` | **sin default: prod no arranca sin esto** |
| `FRONTEND_URL` | CORS |

En el frontend hay que cambiar las **tres** variants juntas (`environment.ts`,
`.development.ts`, `.prod.ts`) por el `fileReplacements` de `angular.json`.

### 5.4 La caché puede confinar

Después de un cambio en BD, `@Cacheable` puede servir datos viejos hasta que se evicte. Si algo "no
aparece", sospechá de la caché antes que del código.

---

## 6. Reglas para trabajar en este repo

1. **No commitear secretos.** Ni `.env`, ni passwords de BD. `application.properties` local no va.
2. **Toda relación se lee dentro de `@Transactional`.** (§2.5)
3. **El check de acceso va en el service**, no en el controller. (§2.5)
4. **Cambiar un ENUM en Java requiere un `ALTER` manual** en cada base. (§2.11)
5. **Si tocás un record DTO, actualizá `models.ts`.** (§2.4)
6. **Si un método escribe, evictá la caché que lo lee.** (§2.9)
7. **No pongas el filtro de usuario adentro de un `@Cacheable`.** (§2.9)
8. **Nada de `@PreAuthorize` para recursos**: va `CampeonatoAccesoService`. (§2.4)
9. **Tests de integración sin `@Transactional`** si querés reproducir el límite transaccional. (§2.12)
10. **Los errores del backend no se tragan**: `ApiError.message` existe para mostrarse. (§4.4)
11. **Correr `verify` antes de pushear**: es lo que corre la CI.

---

## 7. Deuda técnica conocida

| Tema | Detalle |
|---|---|
| Flyway deshabilitado | `ddl-auto=update` no migra ENUMs; las migraciones están escritas pero inactivas |
| `DataSeeder` en prod | corre en cada arranque, incluso en producción. Es idempotente, pero crea usuarios de prueba reales |
| Errores tragados | `catchError(() => of([]))` en varias tabs; impide diagnosticar desde la UI |
| Paginación inconsistente | algunos endpoints devuelven `{content}`, otros array plano; `normalizeList` lo tapa |
| Sin tests de frontend | `ng test` no tiene runner configurado y no hay `.spec.ts` |
| Sin linter de Java | no hay formatter ni checkstyle configurado |
| Faltan datos de producción | el plan de producción nunca llegó a ejecutarse con datos reales |