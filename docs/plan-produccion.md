# Plan de salida a producción — LFM Nacional

> Objetivo: operar la plataforma para una liga real de forma segura y con
> integridad de datos.
> Alcance: lanzamiento seguro (Fases 1-4). **No** incluye features nuevas.
> Estado: plan aprobado, listo para ejecutar en fases.

---

## Contexto

- **Stack**: Spring Boot 4.1.0 / Java 17 / MySQL 8 / JPA / frontend vanilla JS.
- **Código**: sólido. 69 tests pasan (`mvn test` → BUILD SUCCESS). La mayoría de
  los puntos críticos del análisis previo (`docs/analisis-mejoras.md`) ya están
  resueltos: endpoints públicos enumerados, `contrasenaServidor` fuera del DTO,
  ownership checks (IDOR) en controllers, JWT/CORS con defaults restrictivos,
  paginación, cache e índices.
- **Lo que falta** para producción son endurecimiento y operación, no código:
  secretos, migraciones de esquema, backups, observabilidad, CI/CD y HTTPS.

---

## Fase 0 — Decisión de infraestructura

Antes de tocar código hay que decidir dónde correr la app. Lo importante es el
**disco persistente compartido** porque la plataforma necesita MySQL + archivos:

- watcher de sesiones de Assetto Corsa (JSON)
- archivos subidos (setups, imágenes, archivos de carrera)

| Opción | Costo | Ventaja | Desventaja |
|---|---|---|---|
| **Oracle Cloud Free Tier (ARM 4 OCPU / 24GB)** | $0 | Ya existe `setup-oracle-cloud.sh`, gratis, MySQL local | Setup y mantenimiento manuales |
| **VPS genérico (Hetzner / DigitalOcean / Vultr)** | ~5-6 USD/mes | Simple, control total, backups fáciles | Requiere pago |
| **PaaS (Render / Railway / Fly.io)** | 0-7 USD/mes | Deploy con un comando, HTTPS automático | MySQL/archivos más difíciles (disco efímero), menos control |

> **Recomendación**: **Oracle Cloud Free Tier** (ya tenés el script y es gratis)
> o **un VPS Hetzner** (~EUR 4/mes). Ambos dan MySQL + disco persistente, que este
> proyecto necesita. Evitar PaaS puro por el disco efímero y el watcher de sesiones.

El plan es agnóstico de infraestructura salvo donde se indique (deploy/HTTPS).

---

## Fase 1 — Urgente: seguridad y secretos (bloquea la publicación)

Sin esta fase no se publica.

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 1.1 | ✅ Sacar la password de BD del repo | `lfmNacional/src/main/resources/application.properties:5` | Había una password de test local hardcodeada. Reemplazar por `${DB_PASSWORD:}`. **HECHO**: ahora usa `${DB_PASSWORD:}` sin default; README documenta exportar la env var (PowerShell/Linux). |
| 1.2 | ✅ Rotar secretos | — | Regenerar `JWT_SECRETO` (`openssl rand -base64 64`) y password de BD antes de publicar. El código no guarda secretos; falta la rotación operativa (cambiar la password real de MySQL). |
| 1.3 | ✅ Sacar el token JWT de la URL | `controller/SteamController.java` + `service/SteamService.java` + `files/js/auth.js` | **HECHO**: el callback redirige con `?steam=ok&codigo=<uuid>` de un solo uso (TTL 60s, in-memory) y el frontend hace `POST /api/steam/completar` para obtener el JWT. El token nunca queda en historial/logs. |
| 1.4 | ✅ XSS por scheme en frontend | `files/js/*` | **HECHO**: `sanitizeUrl()` en api.js:183 (whitelist http(s)) aplicado a `videoUrl`, `linkPista`, `linkAuto`, `fotoPerfil` y al link de notificaciones (notifications.js `linkDe`). |
| 1.5 | ✅ No loguear datos sensibles | `service/SteamService.java` | **HECHO**: se quitaron `identity`/`guidSteam`/`body` de los logs de Steam; solo se loguea presencia y el id interno. |
| 1.6 | ✅ Rate limiting en login/registro | `security/RateLimitFilter.java` + `config/SecurityConfig.java` | **HECHO** (adicional a la Fase 1): 5 intentos/60s por IP con lockout de 5 min → HTTP 429. Ver Fase 3.5. |

---

## Fase 2 — Integridad de datos (el riesgo silencioso)

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 2.1 | ✅ Migración Flyway V1 | `src/main/resources/db/migration/V1__init.sql` (nuevo) | **HECHO**: schema de las 28 tablas + índices, generado por dump real (`mysqldump --no-data`). |
| 2.2 | ✅ Activar Flyway + `validate` en prod | `application-prod.properties` | **HECHO**: `spring.flyway.enabled=true`, `ddl-auto=validate`, `baseline-on-migrate=true` + `baseline-version=1`. Verificado: subida limpia (V1 aplica) y baseline sobre BD existente sin history. Requiere `spring-boot-starter-flyway` en `pom.xml` (Boot 4 modular). |
| 2.3 | ✅ Mantener dev sin bloqueo | `application.properties` | **HECHO**: dev con `update` y Flyway off; comentario indica generar `V2__…` al cambiar entidades. Tests siguen en H2 con Flyway off. |
| 2.4 | ✅ Backups automatizados | `scripts/backup-mysql.sh` + cron (documentado en README) | **HECHO**: `mysqldump --single-transaction --routines --triggers`, `.sql.gz`, retención 7 días (`RETENTION_DAYS`), credenciales por env. |

---

## Fase 3 — Operación y observabilidad

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 3.1 | ✅ Agregar Spring Boot Actuator | `pom.xml` + `application-prod.properties` + `config/SecurityConfig.java` | **HECHO**: `spring-boot-starter-actuator`; se expone solo `/actuator/health` (público para el HEALTHCHECK, con BD validada) y `show-details=never`. |
| 3.2 | ✅ Healthcheck del Dockerfile | `Dockerfile:37-38` | **HECHO**: `HEALTHCHECK` apuntando a `/actuator/health` via `wget --spider`. |
| 3.3 | ✅ Logging en prod | `application-prod.properties` | **HECHO**: archivo `lfm.log` con rotación (10MB x 7, máx 100MB) en `${LOG_DIR}`, niveles WARN para Hibernate/Hikari/Tomcat, y `server.error.include-stacktrace=never`. |
| 3.4 | ✅ Completar handlers de error | `exception/GlobalExceptionHandler.java` | **HECHO** (ya existía): genérica 500 limpio (sin stack trace en respuesta), `HttpMessageNotReadableException` → 400 `INVALID_JSON`, `MissingServletRequestParameterException` → 400 `MISSING_PARAMETER`. |
| 3.5 | ✅ Rate limiting | `security/RateLimitFilter.java` + `config/SecurityConfig.java` | **HECHO** (se adelantó a la Fase 1): `/login` y `/registro-steam`, 5 intentos/60s por IP, lockout 5 min, sin dependencias. |

---

## Fase 4 — CI/CD y HTTPS

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 4.1 | ✅ CI GitHub Actions | `.github/workflows/ci.yml` (nuevo) | **HECHO**: `./mvnw clean verify` (69 tests) en cada push/PR a `main`, con JDK 17 Temurin y cache Maven. Verificado localmente: BUILD SUCCESS + JAR. |
| 4.2 | ✅ Deploy automatizado | `.github/workflows/deploy.yml` + `scripts/deploy.sh` | **HECHO**: workflow `Deploy` (manual) build → test → SCP del JAR → `deploy.sh` en el server: parar → backup → instalar JAR → arrancar → healthcheck `/actuator/health`. Complementa `setup-oracle-cloud.sh` (JAR + systemd, sin Docker). |
| 4.3 | ✅ HTTPS + proxy | `scripts/Caddyfile` + `scripts/setup-https.sh` + `application-prod.properties` | **HECHO**: Caddy como reverse proxy TLS frente a la app en `:8080` (que ya sirve frontend + `/api`). `setup-https.sh` instala Caddy y genera la config en dos modos: sin dominio (HTTP `:80` funcional) y con `DOMAIN` (Let's Encrypt automático, bloquea `/actuator/*` salvo health). `server.forward-headers-strategy=framework` para redirects https. **Pendiente solo operativo**: dominio + security list 80/443 (cerrar 8080). |

---

## Orden de ejecución

```
1. Fase 1  (secretos + token + XSS)     -> sin esto no se publica
2. Fase 2  (Flyway + backups)           -> antes de cargar datos reales
3. Fase 4.1 (CI)                        -> barato, protege lo ya hecho
4. Fase 3  (Actuator + logging + errores + rate-limit)
5. Fase 4.2 + 4.3 (deploy + HTTPS)      -> último, depende de infra definida
```

## Fuera de alcance (fase 2 de mercado, no bloquea el lanzamiento)

- Features del `requisitos.txt`: puntos de campeonato configurables (hoy
  hardcodeados F1), quorum/desempate de comisarios configurable, notificación
  pre-inicio de carrera, estadísticas admin.
  (RF-028 credenciales de servidor para inscriptos YA implementado:
  `GET /api/carreras/{id}/acceso-servidor`, visible para inscriptos.)
- Cumplimiento/privacidad de datos de pilotos (para Argentina: Ley 25.326).

---

## Nota de despliegue

- La app expone el **watcher de sesiones de Assetto Corsa** y archivos subidos;
  el proceso debe correr con acceso al disco persistente correcto (`SESIONES_DIR`,
  `ARCHIVOS_DIR`, `frontend.resources-path`), ya previstos en `application-prod.properties`.
