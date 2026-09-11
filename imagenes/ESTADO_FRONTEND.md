# Frontend Angular — Late Brake Motorsport

Archivo de estado de implementación. Marca qué falta hacer sobre el frontend nuevo en `frontend/`.

## Estado actual (11/09/2026)

### Hecho
- [x] Proyecto Angular 22 creado en `frontend/` (`ng new` + deps instaladas)
- [x] Design system SCSS con paleta LBM completa:
  - `_tokens.scss` (tokens: rojo LBM `#C61A1A`, fondos `#121212/#1E1E1E/#2A2A2A`, tipografía)
  - `_base.scss` (reset, headings, utilidades, patrones checker/trackgrid)
  - `_components.scss` (botones, brackets, chips, tabs, tables, forms, modales, skeletons, toasts...)
  - `_patterns.scss` (hero, race-card, race-row, detail-header, profile-header, admin-layout, auth-shell)
- [x] `index.html` con branding "Late Brake Motorsport" + fuentes (Rajdhani/Inter/JetBrains Mono)
- [x] `styles.scss` importando los 4 partials
- [x] Modelos TypeScript completos en `core/models/models.ts` (28 entidades/DTOs del backend, con nombres de campos exactos de los DTO Java)

#### 1. Core (services / guards / interceptors) — HECHO
- [x] `core/services/api.service.ts` — wrapper de HttpClient con JWT, errores, paginación y `normalizeList()` (fix paginación inconsistente del viejo)
- [x] `core/services/auth.service.ts` — sesión con signals, `login`, `logout`, `currentUser`, token, redirect post-login con `lfm_next` (fix bug del `next` roto)
- [x] `core/services/toast.service.ts` — sistema de notificaciones emergentes
- [x] `core/interceptors/auth.interceptor.ts` — inyecta `Authorization: Bearer` y maneja 401 → `/auth?next=`
- [x] `core/guards/auth.guard.ts` (authGuard + adminGuard)
- [x] `core/pipes/` — `fecha-relativa`, `fmt-lap`, `fmt-fecha`, `estado-chip` (mapear enum → clase chip)
- [x] `core/directives/has-role.directive.ts` — ocultar/mostrar por rol (fix fuga de info de comisarios)

#### 2. Shared (layout + componentes reutilizables) — HECHO
- [x] `app.ts` / `app.html` / `app.routes.ts` — shell reescrito (rutas lazy, header/footer global, `app.config.ts` con interceptor)
- [x] `shared/components/header/` — navbar con logo `assets/logo.png`, nav links, avatar de sesión, badge notificaciones, menú móvil
- [x] `shared/components/footer/` — footer con columnas de navegación
- [x] Como componentes standalone: `chip`, `rank-badge`, `modal`, `avatar`, `star-rating`, `confirm-dialog`, `toast-container`, `empty-state` (el resto —stat-card, skeletons, feed-item, race-date-badge, tabs, etc.— se implementó con clases del design system dentro de las features)
- [x] Logo copiado a `frontend/src/assets/logo.png` y `frontend/public/logo.png`

#### 3. Features (las 13 pantallas + mejoras) — HECHO
- [x] `home/` — hero dinámico (stats pilotos/carreras/categorias), CTA "Inscribirme", top-3 campeonato, último anuncio + modal
- [x] `auth/` — login/registro, flujo Steam OAuth completo (login, setup nuevo, vincular, callbacks `?steam=*`)
- [x] `races/races-list/` — tabs próximas/pasadas, filtro categoría, cupo
- [x] `races/race-detail/` — info, archivos, inscriptos, resultados, clasificación, análisis vueltas, inscribirse/bajarse
- [x] `championship/` — selector de campeonato + tabla de posiciones con podio
- [x] `categories/` — grid de categorías con rango Elo y conteo de pilotos
- [x] `setups/` — lista con filtros, alta, detalle con rating 1-5, comentarios, descarga
- [x] `profile/driver-profile/` — perfil público: stat-grid 6 stats, historial rating, resultados, logros
- [x] `profile/my-profile/` — datos personales, contraseña, Steam, inscripciones, recompensas
- [x] `notifications/` — feed, marcar leída/no-leída, eliminar
- [x] `achievements/` — grid de logros con progreso
- [x] `incidents/` — reportar, ver incidentes, sanciones, apelaciones, panel comisario (votar, resolver) y admin (sanciones)
- [x] `admin/` — CRUD carreras, campeonatos, categorías, pilotos, anuncios

#### 4. Config / assets — HECHO
- [x] `environments/environment.ts`, `environment.development.ts` y `environment.prod.ts` (URL API)
- [x] `proxy.conf.json` — proxy dev a `http://localhost:8080`
- [x] `angular.json` — reescrito: assets (`public/**` + `src/assets/**`), `fileReplacements` dev/prod, budgets 800kB/1.5MB, proxy dev
- [x] Logo copiado a `src/assets/` y `public/`

#### 5. Deploy — PENDIENTE
- [ ] Dockerfile multi-stage: build Angular → copiar `dist/` a Spring Boot static
- [ ] GitHub Actions: build Angular en CI

### Build
- [x] `npm run build` (ng build) compila sin errores ni warnings. Output en `dist/frontend`.`

## Endpoints backend ya mapeados (para referencia)

Métodos por dominio: `/api/usuarios`, `/api/carreras`, `/api/inscripciones`, `/api/campeonatos`, `/api/categorias`, `/api/setups`, `/api/logros`, `/api/recompensas`, `/api/incidentes`, `/api/sanciones`, `/api/apelaciones`, `/api/votos`, `/api/notificaciones`, `/api/anuncios`, `/api/resultados`, `/api/clasificaciones`, `/api/vueltas`, `/api/archivos`, `/api/imagenes`, `/api/sesiones`, `/api/steam`. Detalle completo en el plan original.

## Bugs del front viejo que el Angular nuevo debe evitar

| Bug viejo | Estado |
|---|---|
| Redirect post-login roto (`next`) | [x] resuelto con `lfm_next` + `router.navigate(returnUrl)` |
| Chips anidados `<span class="chip"><span class="chip">` | [x] resuelto con componente `<app-chip>` |
| Paginación inconsistente (`.content \|\| r`) | [x] resuelto con `normalizeList()` tipado |
| Header/footer duplicados en 13 archivos | [x] resuelto con componentes compartidos |
| Estilos inline masivos | [x] resuelto con component SCSS |
| Datos hardcodeados en home (top-3, carreras) | [x] resuelto con carga dinámica |
| Votos de comisarios visibles a todos | [x] resuelto con `hasRole` directive |
| Tabs-ancla scroll en race-detail | [x] resuelto con tabs reales |
| `posicionFinal` null → asume 9 | [x] manejo explícito |

## Orden sugerido de trabajo

1. Core services + guards + interceptors + pipes + directives
2. Shared components + layout (header/footer/chip/etc)
3. Rutas + app shell
4. auth → profile → races → home
5. championship/categories → setups → notifications/achievements
6. incidents → admin
7. Assets/config/deploy + build final