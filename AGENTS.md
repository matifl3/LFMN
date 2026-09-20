# AGENTS.md

Monorepo para una liga de sim racing (Assetto Corsa). Dos apps independientes:

- `lfmNacional/` — backend Spring Boot 4.1.0 (Java 17, Maven). MySQL en runtime, H2 en tests. Lombok activo.
- `frontend/` — Angular 22 SPA (standalone components + signals, sin NgModules). SCSS.
- `scripts/`, `docs/`, `Dockerfile`, `testJsonServidor/` (JSON de sesiones AC para pruebas de importación).

## Comandos backend (desde `lfmNacional/`)

```bash
./mvnw -B --no-transfer-progress clean verify   # build + tests (H2, sin DB externa)
./mvnw test -Dtest=NombreDelTest               # test único
./mvnw spring-boot:run                          # requiere MySQL corriendo (ver abajo)
```

- No hay linter/format autoconfigurado para Java.
- Test de integración de contexto arranca solo si la clase no levanta DB real; MySQL **no** es necesario para `verify`.

## Comandos frontend (desde `frontend/`)

```bash
npm ci                 # instalar (npm@11 + Node >=20.19)
npm start              # dev en :4200, proxy /api -> http://localhost:8080 (proxy.conf.json)
npm run build          # build production; ES el typecheck (no hay script de typecheck/lint)
npx prettier --write src   # formateo: singleQuote, printWidth 100, parser angular en .html
```

- **No hay tests de frontend**: `ng test` no tiene runner configurado y no existen `.spec.ts`. No los agregues sin confirmar.
- Cambiar una env (`environment*.ts`) requiere cambiar las 3: `environment.ts`, `.development.ts`, `.prod.ts` (fileReplacements en `angular.json`).

## Arquitectura: lo que no se deduce de los nombres

- Backend: `controller/ → service/ → repository/`, DTOs son **records** (`dto/.../XxxResponse`) mapeados en `mapper/EntityMapper.java`. Los nombres de campos camelCase llegan 1:1 al JSON y se reflejan en `frontend/src/app/core/models/models.ts`. **Si tocás un DTO, actualizá la interface TS correspondiente.**
- Frontend: rutas lazy (`loadComponent`) en `app.routes.ts`; cada feature es un directorio en `src/app/features/<feature>/` con `*.component.ts|.html|.scss`. API centralizada en `ApiService` (agrega JWT y `normalizeList` tolera paginación `{content}` o array plano).
- Caché Spring (`@Cacheable`, ej. `CategoriaService.listAll`) puede servir datos viejos tras cambios en BD hasta evictar; tenelo en cuenta si el cambio "no aparece".
- Ingestión de sesiones AC: folder watcher configurado con `sesiones.input-dir` (default `./sesiones`), consume JSON `QUALIFY`/`RACE`, autogenera resultados, Elo/SR e incidentes.

## Gotchas operativos

- **DataSeeder corre en cada arranque, también en prod** (`config/DataSeeder.java`, sin `@Profile`). Es idempotente. Crea `admin@lfm.local / admin123` y pilotos de prueba `piloto1/piloto2@lfm.local / piloto123`, todas las categorías, logros y campeonatos. El README dice "sin DataSeeder en prod" — **está desactualizado**.
- Backend local: `spring.datasource.password=${DB_PASSWORD}` **no tiene default** → falla sin esa variable. Usa usuario `root` por default en `localhost:3306/lfm`.
- Schema: dev y prod usan `ddl-auto=update` (Flyway deshabilitado). Los `V1__`/`V2__` de `db/migration` quedan reservados para un switch futuro; por ahora editar entidades JPA aplica solo.
- **Push a `main` redeployea en producción**: frontend en Vercel (`lfmn.vercel.app`) y API en Render (`lfmn.onrender.com`, API url real: `https://lfmn.onrender.com`). `.github/workflows/deploy.yml` es legacy (servidor propio, manual).
- Prod no arranca sin `JWT_SECRETO`; el healthcheck de mail está deshabilitado (`management.health.mail.enabled=false`) hasta configurar SMTP.
- Nunca comitear `.env`, secretos ni la password de BD.