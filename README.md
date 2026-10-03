# LFM Nacional — Low Fuel Motorsport

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-blue?logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/Licencia-Private-blue)

Plataforma web para una **liga de sim racing** (Assetto Corsa). Gestiona pilotos,
categorías, campeonatos, carreras, un sistema de rating (Elo y Safety Rating),
incidentes con votación de comisarios, sanciones, apelaciones, setups, logros y
recompensas.

## Funcionalidades

- **Cuentas y sesiones**: registro con email/contraseña, login, cambio de contraseña
  y autenticación con **Steam (OAuth)**.
- **Categorías**: con rangos de Elo mínimo/máximo y configuración de setup
  (abierto o fijo).
- **Carreras e inscripciones**: próximas y pasadas, cupo máximo, lista de espera
  con promoción automática, cierre de inscripciones 5 min antes del inicio y
  servidor asignado con contraseña para inscriptos.
- **Resultados e ingestión de sesiones**: importa los JSON que exporta el servidor
  de Assetto Corsa (`QUALIFY` / `RACE`) por carga manual, generando clasificación,
  resultados y autogenerando incidentes por colisión. La importación es
  idempotente por contenido: reenviar el mismo JSON devuelve `409` sin duplicar.
- **Rating**: recálculo automático de **Elo** y **Safety Rating (SR)** al cargar
  resultados, con historial de cambios.
- **Campeonatos**: tabla de posiciones con puntos estilo F1 (25-18-15-12-10-8-6-4-2-1).
- **Incidentes**: reporte con evidencia (video/enlace), asignación a comisarios,
  votación con quórum de 2 votos y resolución con sanción asociada.
- **Sanciones y apelaciones**: penalizaciones por puestos/segundos, ajustes de
  Elo/SR y flujo de apelación resuelto por el admin.
- **Setups**: publicación, descarga, calificación 1-5 estrellas, comentarios y
  búsqueda por circuito/vehículo.
- **Logros y recompensas**: otorgamiento automático al cumplir condiciones,
  progreso visible y recompensas reclamables.
- **Notificaciones y anuncios**: feed con leída/no leída y anuncios publicados
  por el admin.
- **Panel de administración**: ABM de usuarios, categorías, carreras, campeonatos,
  logros y anuncios, más importación de resultados.

## Roles

| Rol | Alcance |
|---|---|
| **USUARIO** | Piloto que participa de la liga: se inscribe, corre y reporta incidentes. |
| **ADMIN** | Administra usuarios, categorías, carreras, campeonatos, logros y anuncios. Resuelve apelaciones. |
| **COMISARIO** | Analiza y vota incidentes, aplica sanciones y ajustes de Elo/SR. |

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 4.1.0 (Java 17, Maven) |
| Seguridad | Spring Security, JWT (jjwt 0.12.6), OAuth Steam, bcrypt |
| Persistencia | Spring Data JPA, MySQL (mysql-connector-j) |
| Frontend | Angular 22 (SPA en `frontend/`) |
| Extra | Ingesta idempotente de sesiones de Assetto Corsa por HTTP |

## Arquitectura y flujos

El sistema se organiza en torno a 7 flujos de negocio: inscripción, carrera →
resultados → rating, incidentes → sanción, apelaciones, setups, logros y
ingestión de sesiones de Assetto Corsa.

Detalles técnicos:

- **[docs/flujos.md](docs/flujos.md)** — diagramas de flujos y modelo de 27 tablas.
- **[docs/formulas-rating.md](docs/formulas-rating.md)** — fórmulas de Elo, SR,
  puntos de campeonato y quórum de comisarios.
- **[requisitos.txt](requisitos.txt)** — especificación de requisitos funcionales
  y no funcionales (RF-001 a RF-100, NFR).

## Capturas

> Capturas de las pantallas del frontend (Angular en `frontend/`):

- Home y próximas carreras
- Login / Registro
- Lista y detalle de carreras
- Campeonato y categorías
- Perfil de piloto y perfil propio
- Setups
- Logros y notificaciones
- Sanciones / Incidentes (panel de comisario)
- Panel de administración

## Despliegue en producción

### Opción 1: Docker (recomendado)

```bash
# Build de la imagen
docker build -t lfm-app .

# Ejecutar con variables de entorno
docker run -d \
  --name lfm \
  -p 8080:8080 \
  -e DB_USERNAME=lfm_user \
  -e DB_PASSWORD=tu_password_seguro \
  -e JWT_SECRETO=tu_jwt_secret_largo \
  -e FRONTEND_URL=https://tudominio.com \
  -e CORS_ALLOWED_ORIGINS=https://tudominio.com \
  -e SPRING_PROFILES_ACTIVE=prod \
  lfm-app
```

### Configuración de producción

| Variable | Descripción |
|---|---|
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Password de MySQL |
| `JWT_SECRETO` | Clave para firmar tokens JWT (generar con `openssl rand -base64 64`) |
| `FRONTEND_URL` | URL pública de la app (ej: `https://tudominio.com`) |
| `CORS_ALLOWED_ORIGINS` | Dominios permitidos (separados por coma) |
| `SPRING_PROFILES_ACTIVE` | Usar `prod` para config segura |
| `JPA_DDL_AUTO` | Default `update` en prod (schema aplicado por Hibernate). `validate` solo si la BD ya existe |
| `FLYWAY_BASELINE` | Reservado para cuando se active Flyway versionado (ver sección Migraciones) |

### Perfiles de Spring

- **default** (desarrollo): `ddl-auto=update`, `show-sql=true`, datos de test,
  Flyway deshabilitado
- **prod** (producción): `ddl-auto=update` (default), `show-sql=false`,
  Flyway deshabilitado, sin DataSeeder

## Migraciones de base de datos (Flyway)

En producción el schema se gestiona por ahora con **Hibernate `ddl-auto=update`**
(flyway deshabilitado) → primer arranque sobre BD vacía crea las 28 tablas solo.

Los archivos `lfmNacional/src/main/resources/db/migration/V*.sql` quedan
reservados para el flujo **Flyway versionado** (follow-up): activar
`spring.flyway.enabled=true` + `JPA_DDL_AUTO=validate` y reinicializar la BD
para que Flyway aplique las migraciones desde cero (una BD creada con
`update` no puede "bastearse" en V1 porque las migraciones V2+ reintentarán
ALTERs ya aplicados). Si se editan entidades JPA mientras siga en `update`,
regenerar diffs con una migración `V3__*.sql` para cuando se haga el switch.

## Deploy (automático con cada push a `main`)

- **Frontend**: Vercel (`lfmn.vercel.app`) conectado al repo en `frontend/`.
- **API**: web service Render (`lfmn.onrender.com`) que buildea el `Dockerfile`
  de la raíz con el perfil `prod`.

Ambos redeployean solos con cada push a `main`. Las variables de producción
(`DB_*`, `JWT_SECRETO`, `FRONTEND_URL`, etc.) se configuran en el dashboard del
proveedor, no en el repo.

## Scripts de utilidad

- `scripts/backup-mysql.sh` — backup de MySQL con `mysqldump`, comprimido en
  `.sql.gz` y retención de 7 días (configurable con `RETENTION_DAYS`).

### Backup automático

`scripts/backup-mysql.sh` se puede agendar con cron en el host donde corre
MySQL, pasando las credenciales por env:

```
0 3 * * * DB_PASSWORD='tu_password' DB_NAME=lfm /ruta/a/scripts/backup-mysql.sh >> /ruta/a/logs/backup.log 2>&1
```

Los backups quedan en la ruta de `BACKUP_DIR` (por defecto junto al script).
Se recomienda copiarlos también fuera del host (ej. a un bucket/DR).

## Pendientes (TBD)

El modelo de requisitos tiene ambigüedades pendientes de validar con el cliente
(detalles en la sección "PENDIENTES TBD" de `requisitos.txt`): umbrales de Elo
por categoría, visibilidad de la contraseña del servidor, fórmula oficial de Elo,
quórum de comisarios, entre otros. Los valores por defecto actuales están
documentados en `docs/formulas-rating.md`.

## Licencia

Proyecto privado. Uso restringido.
