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
- Los tests corren sobre H2 in-memory (`src/test/resources/application.properties`, `MODE=MYSQL`, `create-drop`): MySQL **nunca** es necesario para `verify`.
- El código vive bajo el paquete `org.example.lfmnacional` (no `com.lowfuel.*`, a pesar del groupId Maven).

## Comandos frontend (desde `frontend/`)

```bash
npm ci                 # instalar (npm@11 + Node >=20.19)
npm start              # dev en :4200, proxy /api -> http://localhost:8080 (proxy.conf.json)
npm run build          # build production; ES el typecheck (no hay script de typecheck/lint)
npx prettier --write src   # formateo: singleQuote, printWidth 100, parser angular en .html
```

- **No hay tests de frontend**: no existen `.spec.ts` y los schematics tienen `skipTests: true` en `angular.json` (todo lo generado sale sin spec). `ng test` no tiene runner configurado. No los agregues sin confirmar.
- Cambiar una env (`environment*.ts`) requiere cambiar las 3: `environment.ts`, `.development.ts`, `.prod.ts` (fileReplacements en `angular.json`).
- **El CI (`.github/workflows/ci.yml`) solo buildea el backend**: el frontend no se typechequea en PRs. Corré `npm run build` a mano para verificar cambios de Angular antes de push.

## Arquitectura: lo que no se deduce de los nombres

- Backend: `controller/ → service/ → repository/`, DTOs son **records** (`dto/.../XxxResponse`) mapeados en `mapper/EntityMapper.java`. Los nombres de campos camelCase llegan 1:1 al JSON y se reflejan en `frontend/src/app/core/models/models.ts`. **Si tocás un DTO, actualizá la interface TS correspondiente.**
- Frontend: rutas lazy (`loadComponent`) en `app.routes.ts`; cada feature es un directorio en `src/app/features/<feature>/` con `*.component.ts|.html|.scss`. API centralizada en `ApiService` (agrega JWT y `normalizeList` tolera paginación `{content}` o array plano).
- Caché Spring (`@Cacheable`, ej. `CategoriaService.listAll`) puede servir datos viejos tras cambios en BD hasta evictar; tenelo en cuenta si el cambio "no aparece".
- Ingestión de sesiones AC: **carga manual** vía `POST /api/sesiones/importar?carreraId=N` (roles ADMIN/COMISARIO/ADMIN_CAMPEONATO). No hay folder watcher: la app no depende de disco local (en Render es efímero). Idempotente por contenido (`sesion_procesada.clave`, SHA-256); reenviar el mismo JSON devuelve `409`; un JSON corregido de una sesión ya importada también es `409` (`OTRA_SESION_IMPORTADA`) y se reemplaza con `POST /api/sesiones/reimportar` (ADMIN/COMISARIO, ver §2.8 de `docs/arquitectura-y-patrones.md`). Los incidentes autogenerados deduplican por `incidente.clave_origen`, así que un reintento no duplica colisiones.

## Gotchas operativos

- **DataSeeder corre en cada arranque, también en prod** (`config/DataSeeder.java`, sin `@Profile`). Es idempotente. Siempre crea categorías, logros y campeonatos. El admin `admin@lfm.local` usa `app.seed.admin-password` (dev: `admin123`; prod: `ADMIN_PASSWORD`, **sin default**): en prod, sin esa variable no se crea, y si ya existe con `admin123` queda deshabilitado (con la variable, se rota a ella). Los pilotos de prueba (`piloto1/piloto2/organizador@lfm.local / piloto123`) solo se crean con `app.seed.usuarios-demo=true` (dev); en prod los existentes con esa password se deshabilitan. El README dice "sin DataSeeder en prod" — **está desactualizado**.
- Backend local: `spring.datasource.password=${DB_PASSWORD}` **no tiene default** → falla sin esa variable. Usa usuario `root` por default en `localhost:3306/lfm`.
- Schema: dev y prod usan `ddl-auto=update` (Flyway deshabilitado, starter igual presente en el pom). Los `V*.sql` de `db/migration` (hoy V1–V3) quedan reservados para un switch futuro; por ahora editar entidades JPA aplica solo.
- **Push a `main` redeployea en producción**: frontend en Vercel (`lfmn.vercel.app`) y API en Render (`lfmn.onrender.com`, API url real: `https://lfmn.onrender.com`). El único workflow es `.github/workflows/ci.yml` (solo CI).
- Prod no arranca sin `JWT_SECRETO`; el healthcheck de mail está deshabilitado (`management.health.mail.enabled=false`) hasta configurar SMTP.
- El `Dockerfile` raíz buildea **solo el backend** (`mvnw package -DskipTests`) y el ENTRYPOINT fuerza `--spring.profiles.active=prod`; no usa el frontend de `frontend/`.
- Nunca comitear `.env`, secretos ni la password de BD.

## Antes de tocar código de negocio, leé

- `docs/arquitectura-y-patrones.md` — reglas explícitas para no repetir los bugs ya encontrados (lazy loading, caché vs. autorización, etc.). Es el contrato de diseño del repo.