# Plan de salida a producción — LFM Nacional

> Objetivo: operar la plataforma para una liga real de forma segura y con
> integridad de datos.
> Alcance: lanzamiento seguro (Fases 1-4). **No** incluye features nuevas.
> Estado: plan aprobado, listo para ejecutar en fases.

---

## Contexto

- **Stack**: Spring Boot 4.1.0 / Java 17 / MySQL 8 / JPA / frontend vanilla JS.
- **Código**: sólido. 54 tests pasan (`mvn test` → BUILD SUCCESS). La mayoría de
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
| 1.1 | Sacar la password de BD del repo | `lfmNacional/src/main/resources/application.properties:5` | Hay `spring.datasource.password=Okapis2205.` hardcodeado (valor de test local). Reemplazar por `${DB_PASSWORD:}`. |
| 1.2 | Rotar secretos | — | Aunque sea test local, la password y el JWT ya pasaron por git. Regenerar `JWT_SECRETO` (`openssl rand -base64 64`) y password de BD antes de exponer nada. |
| 1.3 | Sacar el token JWT de la URL | `controller/SteamController.java:47` + `service/SteamService.java` | El callback redirige con `?token=...` y queda en historial/logs. Cambiar a cookie `HttpOnly + SameSite=Lax` o a un código de un solo uso intercambiado por `POST`. |
| 1.4 | XSS por scheme en frontend | `files/*/api.js`, `incidents.js`, `race-detail.js` | `esc()` no neutraliza `javascript:` en URLs de usuario. Whitelist de schemes `http(s)`. |
| 1.5 | No loguear datos sensibles | `service/SteamService.java:109` | El `log.info` del callback incluye `identity` (SteamID) en los logs. Sanitizar. |

---

## Fase 2 — Integridad de datos (el riesgo silencioso)

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 2.1 | Migración Flyway V1 | `src/main/resources/db/migration/V1__init.sql` (nuevo) | Replicar el schema de las 28 tablas + índices actuales. |
| 2.2 | Activar Flyway + `validate` en prod | `application-prod.properties:14-20` | Hoy usa `ddl-auto=update` con `flyway.enabled=false`. Poner `spring.flyway.enabled=true` y `ddl-auto=validate`: si el código y la BD no coinciden, **falla en startup**, no en runtime. |
| 2.3 | Mantener dev sin bloqueo | `application.properties` | Dev sigue con `update` y Flyway off. |
| 2.4 | Backups automatizados | script + cron (nuevo) | `mysqldump` diario + retención N días + dump previo a cada deploy. El historial de Elo/SR no se reconstruye; sin backup una corrupción es pérdida total. |

---

## Fase 3 — Operación y observabilidad

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 3.1 | Agregar Spring Boot Actuator | `pom.xml` + `application-prod.properties` | Dependencia `spring-boot-starter-actuator`; exponer `/actuator/health` validando la BD. |
| 3.2 | Healthcheck del Dockerfile | `Dockerfile:37-38` | Apuntar el `HEALTHCHECK` a `/actuator/health` en lugar de `/`. |
| 3.3 | Logging en prod | `application-prod.properties` | Configurar `logging.level` (WARN/INFO), rotación de archivos, sin secrets. |
| 3.4 | Completar handlers de error | `exception/GlobalExceptionHandler.java` | Añadir handlers: excepción genérica (500 limpio sin stack trace), `HttpMessageNotReadableException` (JSON malformado), `MissingServletRequestParameterException`. |
| 3.5 | Rate limiting | `config/SecurityConfig.java` + código | Limitar `/login` y `/registro` para evitar fuerza bruta sobre cuentas de pilotos. |

---

## Fase 4 — CI/CD y HTTPS

| # | Tarea | Archivo(s) | Detalle |
|---|---|---|---|
| 4.1 | CI GitHub Actions | `.github/workflows/ci.yml` (nuevo) | `mvn clean verify` en cada push/PR (ejecuta los 54 tests). Sin deploy todavía. |
| 4.2 | Deploy automatizado | script + `Dockerfile` | Build → test → imagen → redeploy controlado: parar → backup → migrar → arrancar. Complementa `setup-oracle-cloud.sh`. |
| 4.3 | HTTPS + proxy | conf según infra | Let's Encrypt + redirección 80→443, con Caddy/Nginx por delante de la app. URLs de BD/frontend/CORS por https. |

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

- Features del `requisitos.txt`: RF-028 (credenciales de servidor para inscriptos),
  puntos de campeonato configurables (hoy hardcodeados F1), quorum/desempate de
  comisarios configurable, notificación pre-inicio de carrera, estadísticas admin.
- Cumplimiento/privacidad de datos de pilotos (para Argentina: Ley 25.326).

---

## Nota de despliegue

- La app expone el **watcher de sesiones de Assetto Corsa** y archivos subidos;
  el proceso debe correr con acceso al disco persistente correcto (`SESIONES_DIR`,
  `ARCHIVOS_DIR`, `frontend.resources-path`), ya previstos en `application-prod.properties`.
