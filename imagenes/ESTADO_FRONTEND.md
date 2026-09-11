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

### Falta (por hacer)
Toda la lógica de la app. El proyecto compila solo con el shell vacío de Angular por ahora.

#### 1. Core (services / guards / interceptors)
- [ ] `core/services/api.service.ts` — wrapper de HttpClient con JWT, errores, paginación y `normalizeList()` (fix paginación inconsistente del viejo)
- [ ] `core/services/auth.service.ts` — sesión con signals, `login`, `logout`, `currentUser`, token, redirect post-login (fix bug del `next` roto)
- [ ] `core/services/toast.service.ts` — sistema de notificaciones emergentes
- [ ] `core/interceptors/auth.interceptor.ts` — inyecta `Authorization: Bearer` y maneja 401
- [ ] `core/guards/auth.guard.ts` y `core/guards/admin.guard.ts`
- [ ] `core/pipes/` — fechas relativas, `fmtLap`, chips de estado (mapear enum → clase chip)
- [ ] `core/directives/has-role.directive.ts` — ocultar/mostrar por rol (fix fuga de info de comisarios)

#### 2. Shared (layout + componentes reutilizables)
- [ ] `app.ts` / `app.html` / `app.routes.ts` — reescribir el shell (rutas lazy, header/footer global)
- [ ] `shared/components/header/` — navbar con logo `imagenes/logo.jpg`, nav links, avatar de sesión, badge notificaciones, menú móvil
- [ ] `shared/components/footer/` — footer con columnas de navegación
- [ ] `shared/components/chip/`, `stat-card/`, `rank-badge/`, `progress-bar/`, `skeleton/`, `modal/`, `tabs/`, `avatar/`, `feed-item/`, `empty-state/`, `race-date-badge/`, `star-rating/`, `bracket-card/`, `confirm-dialog/`, `data-table/`, `toast-container/`
- [ ] Copiar `imagenes/logo.jpg` a `frontend/src/assets/`

#### 3. Features (las 13 pantallas + mejoras)
- [ ] `home/` — hero dinámico (stats pilotos/carreras/categorias), CTA "Inscribirme", top-3 campeonato, último anuncio + modal
- [ ] `auth/` — login/registro, flujo Steam OAuth completo
- [ ] `races/` — lista (tabs próximas/pasadas, filtro categoría, cupo) + detalle (info, archivos, inscriptos, resultados, clasificación, análisis vueltas, inscribirse/bajarse)
- [ ] `championship/` — selector de campeonato + tabla de posiciones con podio
- [ ] `categories/` — grid de categorías con rango Elo y conteo de pilotos
- [ ] `setups/` — lista con filtros, alta con drag&drop, detalle con rating 1-5, comentarios, descarga
- [ ] `profile/driver-profile/` — perfil público: stat-grid 6 stats, gráficos Elo/SR, resultados, logros
- [ ] `profile/my-profile/` — datos personales, contraseña, Steam, inscripciones, recompensas
- [ ] `notifications/` — feed, marcar leída/no-leída, eliminar
- [ ] `achievements/` — grid de logros con progreso
- [ ] `incidents/` — reportar, ver incidentes, sanciones, apelaciones, panel comisario (votar, resolver) y admin (sanciones) — el más complejo (671 líneas en el viejo)
- [ ] `admin/` — CRUD carreras, campeonatos, categorías, pilotos, anuncios + importar sesiones

#### 4. Config / assets
- [ ] `environments/environment.ts` + `environment.prod.ts` (URL API)
- [ ] `proxy.conf.json` — proxy dev a `http://localhost:8080`
- [ ] `angular.json` — configurar assets (logo), build output
- [ ] Copy logo a `src/assets/`

#### 5. Deploy
- [ ] Dockerfile multi-stage: build Angular → copiar `dist/` a Spring Boot static
- [ ] GitHub Actions: build Angular en CI

## Endpoints backend ya mapeados (para referencia)

Métodos por dominio: `/api/usuarios`, `/api/carreras`, `/api/inscripciones`, `/api/campeonatos`, `/api/categorias`, `/api/setups`, `/api/logros`, `/api/recompensas`, `/api/incidentes`, `/api/sanciones`, `/api/apelaciones`, `/api/votos`, `/api/notificaciones`, `/api/anuncios`, `/api/resultados`, `/api/clasificaciones`, `/api/vueltas`, `/api/archivos`, `/api/imagenes`, `/api/sesiones`, `/api/steam`. Detalle completo en el plan original.

## Bugs del front viejo que el Angular nuevo debe evitar

| Bug viejo | Estado |
|---|---|
| Redirect post-login roto (`next`) | [ ] por resolver con `router.navigate(returnUrl)` |
| Chips anidados `<span class="chip"><span class="chip">` | [ ] por componente `<app-chip>` |
| Paginación inconsistente (`.content \|\| r`) | [ ] con `normalizeList()` tipado |
| Header/footer duplicados en 13 archivos | [x] solución con componentes compartidos (falta crearlos) |
| Estilos inline masivos | [x] solución con component SCSS (falta aplicarlos) |
| Datos hardcodeados en home (top-3, carreras) | [ ] por carga dinámica |
| Votos de comisarios visibles a todos | [ ] con `hasRole` directive |
| Tabs-ancla scroll en race-detail | [ ] con tabs reales |
| `posicionFinal` null → asume 9 | [ ] manejo explícito |

## Orden sugerido de trabajo

1. Core services + guards + interceptors + pipes + directives
2. Shared components + layout (header/footer/chip/etc)
3. Rutas + app shell
4. auth → profile → races → home
5. championship/categories → setups → notifications/achievements
6. incidents → admin
7. Assets/config/deploy + build final