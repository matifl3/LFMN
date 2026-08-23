# Analisis y Mejoras - LFM Nacional

> Ultima actualizacion: 2026-08-23

---

## Estado General

Proyecto de plataforma web para liga de sim racing (Assetto Corsa) con Spring Boot 4.1.0 + Java 17 + MySQL + frontend vanilla JS.

**Escala**: 28 entidades, 27 controllers, 29 services, 28 repositories, ~70 DTOs, 15 modulos JS.

**Veredicto**: Codigo bien estructurado y legible, dominio completo. Vulnerabilidades de seguridad criticas (IDOR + GET publico) y cero tests que resolver antes de produccion.

---

## 1. SEGURIDAD

### 1.1 Criticos

- [x] **GET publico masivo** -- SecurityConfig.java:36 tiene requestMatchers(GET, /**).permitAll() que expone usuarios, passwords de servidor, incidentes y setups a anonimos
  - Fix: Eliminar wildcard y enumerar explicitamente los endpoints publicos (categorias, campeonatos basicos, anuncios)
- [x] **Password del servidor visible** -- CarreraResponse.java:19 incluye contrasenaServidor en todas las respuestas de carrera
  - Fix: Quitar del DTO publico, crear endpoint autenticado GET /api/carreras/{id}/credenciales que verifique inscripcion activa (RF-028/NFR-005)
  - Nota: DTO limpio. Falta endpoint autenticado para que inscriptos vean la password en la UI (race-detail.js no la muestra actualmente).
- [x] **IDOR generalizado** -- cualquier usuario puede cambiar passwords, perfiles, setups y notificaciones de otros usuarios
  - Fix: Agregar ownership checks usando @AuthenticationPrincipal en todos los endpoints afectados (UsuarioController, SetupController, NotificacionController, InscripcionController, IncidenteController)

### 1.2 Altos

- [x] **Asignacion masiva en incidentes** -- IncidenteRequest.java:12 permite crear incidentes ya RESUELTO; SetupRequest.autorId permite elegir el autor
  - Fix: Ignorar campos de estado/autor en el request, derivarlos del contexto del usuario autenticado
- [x] **JWT secret con valor por defecto** -- application.properties:32 tiene un secret predecible; si no se setea la env var, se puede forjar tokens admin
  - Fix: Quitar default, forzar que el startup falle si JWT_SECRETO no esta configurado
- [x] **CORS abierto + credenciales** -- WebConfig.java:32 usa allowedOriginPatterns(*) con allowCredentials(true); default en properties es *
  - Fix: Default restrictivo en application.properties, solo * en dev
- [x] **Sin validacion de tipo de archivo** -- SetupService.guardarArchivo() (linea 124) acepta cualquier extension hasta 10MB, incluyendo .html/.svg (XSS almacenado)
  - Fix: Whitelist de extensiones MIME permitidas (.ini, .acd, .json, .rar, .zip)
- [x] **Open redirect en login** -- auth.js:11-14 usa next del query string sin validar
  - Fix: Validar que next empiece con / (relativo)
- [ ] **Token JWT en URL** -- Steam callback devuelve token como query param, quedando en historial y logs
  - Fix: Usar cookie de sesion temporal o codigo intercambiado via POST

### 1.3 Medios

- [ ] **XSS por scheme** -- esc() no neutraliza javascript: en URLs (incidents.js:85, race-detail.js:81, api.js:228)
  - Fix: Whitelist de schemes http(s) en URLs de usuario
- [ ] **Session localStorage** -- XSS-stealable; aceptable para este proyecto pero sin CSP headers
  - Fix futuro: Agregar headers CSP

---

## 2. PERFORMANCE

### 2.1 Criticos

- [ ] **Cero paginacion** -- 17 llamadas a findAll().stream()...toList() en todo el backend. Viola NFR-003
  - Fix: Agregar Pageable a todos los endpoints de listado, empezando por historiales, resultados y carreras
  - Archivos: CarreraService.java:67, UsuarioService.java:105, IncidenteService.java:52, LogroService.java:56

### 2.2 Altos

- [ ] **N+1 en logros** -- evaluarLogros (LogroService.java:135-159) hace ~300 queries para 30 pilotos x 7 logros
  - Fix: Batch load de logros y UsuarioLogro existentes en una sola query
- [ ] **N+1 en incidentes** -- asignarPilotos (IncidenteService.java:90-104) re-fetchea TODOS los pilotos del incidente por cada piloto nuevo
  - Fix: Cachear pilotos del incidente en memoria durante la operacion

### 2.3 Medios

- [ ] **Guardado individual en bulk** -- save() dentro de loops en vez de saveAll()
  - Archivos: ResultadoCarreraService.java:91,129,134, CampeonatoService.java:136, SancionService.java:272-278
- [ ] **Job cada minuto escanea todas las carreras** -- cerrarInscripcionesAutomaticamente carga todas las carreras de dos estados y filtra fechas en Java
  - Fix: Query findByEstadoInAndFechaBefore en vez de traer todo
- [ ] **Sin cache** -- categorias, campeonatos y logros se consultan a DB en cada request; home carga 5 endpoints
  - Fix: @Cacheable en datos estaticos/referencia
- [ ] **N+1 en admin** -- admin.js:69-74 hace 1 request por carrera para contar inscriptos
  - Fix: Agregar count al CarreraResponse o crear endpoint batch

---

## 3. CALIDAD DE CODIGO

### 3.1 Duplicacion

- [ ] **~30 mappers toResponse() casi identicos** -- la cadena getCarrera().getCampeonato().getCategoria().getNombre() se repite en 5+ services
  - Fix: Mapper centralizado o utility method
- [ ] **Tablas duplicadas de rating** -- EloSancion y SafetyRatingSancion son identicas
  - Fix: Una sola tabla rating_cambio con campo tipo discriminador
- [ ] **Elo/SR history mappers** -- toEloHistorial y toSafetyRatingHistorial en UsuarioService.java:230-246 son estructuralmente identicos

### 3.2 Complejidad

- [ ] **Servicio god-mode** -- SesionServidorService.java (442 lineas) mezcla parsing, resolucion, importacion y generacion de incidentes
  - Fix: Dividir en 3-4 servicios mas pequenos
- [ ] **Delete fragil** -- CarreraService.delete() (linea 152-171) elimina de 13 repos manualmente en vez de cascadas DB
  - Fix: Usar cascade = CascadeType.ALL o ON DELETE CASCADE en las relaciones necesarias

### 3.3 Errores

- [ ] **Manejo incompleto** -- sin handler para Exception generica (500s con stack trace), HttpMessageNotReadableException (JSON malformado), MissingServletRequestParameterException
  - Fix: Agregar handlers en GlobalExceptionHandler.java

### 3.4 Inconsistencias

- [ ] **Dependencia duplicada** -- spring-boot-starter-validation declarada 2 veces en pom.xml (lineas 41-44 y 93-96)
- [ ] **Codigo muerto** -- SteamService.crearConSteam() (linea 149-156) nunca se llama
- [ ] **Logica duplicada frontend/backend** -- quorum hardcoded = 2 tanto en IncidenteService.java:30 como en incidents.js:190
- [ ] **sistemaPuntos decorativo** -- el texto se guarda en Campeonato.java:40 pero los puntos estan hardcodeados en CampeonatoService.java:26-27

---

## 4. BASE DE DATOS

### 4.1 Performance

- [ ] **Cero indices secundarios** -- ninguna de las 28 entidades tiene @Index
  - Indices prioritarios: carrera(fecha, estado), resultado_carrera(usuario_id, posicion_final), elo_sancion(usuario_id, fecha), setup(circuito, vehiculo)

### 4.2 Migraciones

- [ ] **Sin Flyway/Liquibase** -- ddl-auto=update en dev, migraciones manuales (ya existe docs/migracion_carrera_campeonato.sql)
  - Fix: Adoptar Flyway para control de versiones de schema

### 4.3 Modelado

- [ ] **Sin @Version** -- sin optimistic locking en filas calientes (Usuario.elo, Usuario.safetyRating)
  - Fix: Agregar @Version a Usuario.java
- [ ] **posicion almacenada y recalculada** -- CampeonatoPosicion.posicion se guarda pero se recalcula en Java; invite a drift
  - Fix: Calcular en tiempo de lectura o tratar como cache
- [ ] **Boolean nullable confuso** -- poles, finalizo en ResultadoCarrera.java:43 son nullable, forzando null-checks por todos lados
  - Fix: Cambiar a boolean primitivo con default false

---

## 5. FRONTEND

### 5.1 Seguridad

- [ ] **XSS por scheme** -- esc() no neutraliza javascript: en URLs de usuario (incidents.js:85, race-detail.js:81, api.js:228)
  - Fix: Whitelist de schemes http(s) en URLs de usuario
- [ ] **Client-side-only authorization** -- admin panel gating es solo DOM-based (admin.js:22-33); un usuario normal puede llamar los endpoints directamente
  - Fix: Backend @PreAuthorize ya protege escrituras admin; agregar ownership checks server-side

### 5.2 UX

- [ ] **Recarga completa de pagina** -- location.reload() despues de inscribirse/baja (race-detail.js:214,225) en vez de re-renderizar estado
  - Fix: Actualizar el DOM con la nueva estado
- [ ] **Fallos silenciosos** -- multiples .catch(function(){}) que tragan errores sin feedback (home.js:94,135, race-detail.js:135,157,183)
  - Fix: Mostrar placeholder o toast de error
- [ ] **Sin loading/estados deshabilitados** -- auth.js deshabilita botones durante submit (bien) pero race-detail no previene doble-click
  - Fix: Agregar debounce/loading a todos los botones de accion
- [ ] **Session nunca expira client-side** -- token expira a 8h pero requireAuth() solo verifica presencia del user object, no validez del token
  - Fix: Verificar expiracion del token en requireAuth()

### 5.3 Performance

- [ ] **N+1 en admin** -- admin.js:69-74 dispara 1 GET /inscripciones/carrera/{id}/count por fila de carrera
  - Fix: Agregar count al CarreraResponse o crear endpoint batch
- [ ] **Home carga 5 endpoints** en paralelo al abrir (home.js:51-57) sin cache
  - Fix: @Cacheable en backend o localStorage con TTL en frontend

---

## 6. FEATURES FALTANTES vs requisitos.txt

### 6.1 Rotas (no funcionan como deberian)

| Requisito | Estado | Detalle |
|---|---|---|
| RF-028 / NFR-005 | **ROTO** | Password de servidor visible publicamente, sin control de "visible X min antes" |
| RF-075/076 | **ROTO** | Sistema de puntos hardcodeado F1, sistemaPuntos es texto decorativo |

### 6.2 Faltantes (no implementadas)

| Requisito | Descripcion |
|---|---|
| RF-038/039 | Elo estimado antes de la carrera (endpoint que proyecte cambios) |
| RF-061 | Notificar inscriptos antes del inicio de carrera |
| RF-072 | Asignar pilotos a servidores (carrera.servidor es un string unico) |
| RF-085 | Estadisticas globales para admin (usuarios activos, carreras, incidentes) |
| NFR-001 | Expiracion de sesion por inactividad (solo expira a las 8h fijas) |

### 6.3 Parciales (implementadas a medias)

| Requisito | Estado actual |
|---|---|
| RF-007 | Foto de perfil: fotoPerfil es string libre, sin upload ni validacion |
| RF-042 | Analisis por vuelta: vueltas se importan pero sin diferencia con lider ni evolucion |
| RF-053 | Video como evidencia: solo videoUrl (enlace), sin upload de archivo |
| RF-066 | ABM usuarios con auditoria: altas/bajas existen, pero sin log de ediciones |
| RF-086 | Moderacion de setups: delete generico existe sin checks de autor |
| RF-087 | Posponer carrera + notificar: solo cancelar existe, no reschedule |
| RF-099 TBD-6 | Quorum configurable/desempate: quorum fijo = 2, sin logica de desempate |
| NFR-007 | Auditoria con responsable: historial Elo/SR no registra quien aplico la sancion |

---

## 7. TESTS

### 7.1 Estado actual

- LfmNacionalApplicationTests.java -- smoke test de context load (sin MySQL falla)
- SesionServerDataTest.java -- 2 tests de parsing JSON de sesiones (buenos)
- **Total: 2 archivos, ~3 tests**

### 7.2 Tests faltantes (por prioridad)

| Prioridad | Tipo de test | Que testear |
|---|---|---|
| 1 | **Seguridad** | Anonimo no accede datos sensibles, usuario A no modifica usuario B, password solo visible para inscriptos |
| 2 | **Unitarios calculadoras** | EloCalculator y SrCalculator son funciones puras, trivialmente testeables, son el core del negocio |
| 3 | **Services mockeados** | InscripcionService (cupos, lista espera), SancionService (apply/revert), CampeonatoService (puntos, posiciones) |
| 4 | **Repository** | countVueltaRapidaByUsuario (JPQL correlacionado complejo) |
| 5 | **Controller/API** | Validacion de errores, status codes, forma del error envelope |
| 6 | **Integracion watcher** | SesionFolderWatcher con @TempDir |
| 7 | **CI/CD** | GitHub Actions o similar con build + tests automaticos |

---

## 8. DEPLOYMENT

### 8.1 Completado

- [x] Secrets movidos a variables de entorno
- [x] application-prod.properties creado
- [x] CORS configurable
- [x] DataSeeder excluido en perfil prod
- [x] Dockerfile multi-stage
- [x] setup-oracle-cloud.sh
- [x] .env.example
- [x] .gitignore actualizado
- [x] README actualizado

### 8.2 Pendiente

- [ ] Elegir nombre para la plataforma
- [ ] Actualizar titulo en HTML y README
- [ ] Agregar logo
- [ ] Crear cuenta Oracle Cloud Free Tier
- [ ] Deployar y probar en produccion
- [ ] Configurar dominio (opcional)
- [ ] Google AdSense (opcional)

---

## PRIORIZACION

### Tabla de esfuerzo vs impacto

| # | Mejora | Esfuerzo | Impacto | Modulo |
|---|---|---|---|---|
| 1 | Eliminar GET publico masivo | Medio | Critico | Seguridad |
| 2 | Quitar contrasenaServidor de DTO | Bajo | Critico | Seguridad |
| 3 | Agregar ownership checks (IDOR) | Medio | Critico | Seguridad |
| 4 | Paginacion en listados | Alto | Critico | Performance |
| 5 | Flyway + indices secundarios | Alto | Alto | BD |
| 6 | Batch de logros (N+1) | Medio | Alto | Performance |
| 7 | Secret JWT sin default + CORS restrictivo | Bajo | Alto | Seguridad |
| 8 | Tests de seguridad + calculadoras | Alto | Alto | Calidad |
| 9 | Validar tipos de archivo + scheme URLs | Bajo | Alto | Seguridad |
| 10 | Features faltantes (Elo, notif, puntos) | Alto | Medio | Funcionalidad |

### Fases sugeridas

**Fase 1 - Seguridad (URGENTE)**
> Seguridad critica antes de cualquier publicacion
> Items: 1, 2, 3, 7, 9

**Fase 2 - Performance**
> Paginacion y N+1 para soportar datos reales
> Items: 4, 5, 6

**Fase 3 - Calidad**
> Tests y refactoring
> Item: 8

**Fase 4 - Features**
> Completar requisitos faltantes
> Item: 10
