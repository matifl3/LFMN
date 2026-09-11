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
- **Resultados e ingestión de sesiones**: importa automáticamente los JSON que
  exporta el servidor de Assetto Corsa (`QUALIFY` / `RACE`) mediante un watcher
  de carpeta, generando clasificación, resultados y autogenerando incidentes por
  colisión.
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
| Extra | Folder watcher de sesiones de Assetto Corsa |

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

### Opción 2: Oracle Cloud Free Tier (gratis forever)

El proyecto incluye un script de setup automático:

```bash
# 1. Crear instancia VM en Oracle Cloud (ARM, 4 OCPU, 24GB RAM, Ubuntu)
# 2. Conectarse por SSH
ssh -i tu-clave ubuntu@ip-publica

# 3. Subir el proyecto
scp -r lfmNacional/ ubuntu@ip:~/
scp -r scripts/ ubuntu@ip:~/app/

# 4. Ejecutar setup
sudo bash setup-oracle-cloud.sh

# 5. Subir el JAR compilado
cd lfmNacional
./mvnw clean package -DskipTests
scp target/lfmNacional-0.0.1-SNAPSHOT.jar ubuntu@ip:~/app/app.jar

# 6. Iniciar
sudo systemctl start lfm
```

El script genera credenciales aleatorias, crea la BD MySQL y configura el servicio systemd.

### Configuración de producción

| Variable | Descripción |
|---|---|
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Password de MySQL |
| `JWT_SECRETO` | Clave para firmar tokens JWT (generar con `openssl rand -base64 64`) |
| `FRONTEND_URL` | URL pública de la app (ej: `https://tudominio.com`) |
| `CORS_ALLOWED_ORIGINS` | Dominios permitidos (separados por coma) |
| `SESIONES_DIR` | Ruta a la carpeta de sesiones de Assetto Corsa |
| `SPRING_PROFILES_ACTIVE` | Usar `prod` para config segura |
| `JPA_DDL_AUTO` | `validate` en prod (default); no usar `update` con Flyway activo |
| `FLYWAY_BASELINE` | `true` (default) para bastear una BD existente sin migrations aplicadas |

### Perfiles de Spring

- **default** (desarrollo): `ddl-auto=update`, `show-sql=true`, datos de test,
  Flyway deshabilitado
- **prod** (producción): `ddl-auto=validate`, `show-sql=false`, Flyway habilitado,
  sin DataSeeder

## Migraciones de base de datos (Flyway)

En **producción** el schema se gestiona con **Flyway** a partir de los archivos
`lfmNacional/src/main/resources/db/migration/V*.sql`:

- `V1__init.sql` contiene el schema completo (28 tablas).
- `spring.flyway.enabled=true` y `JPA_DDL_AUTO=validate` en el perfil `prod`.
- `FLYWAY_BASELINE=true` (default) con `baseline-version=1`: si la BD ya existe
  pero no tiene la tabla `flyway_schema_history` (schema creado por Hibernate
  con `ddl-auto=update`), Flyway la "bastea" marcando V1 como ya aplicado y no
  re-crea las tablas.
- Si cambiás entidades JPA, generá una nueva migración `V2__*.sql` (y siguientes)
  para mantener el schema consistente en prod. En desarrollo local podés
  seguir usando `ddl-auto=update` sin migraciones.

No se necesita configuración especial de primera subida: sobre una **BD nueva**
Flyway aplica V1 y `validate` confirma el schema.

## Deploy automatizado (GitHub Actions)

El workflow `.github/workflows/deploy.yml` publica una versión y hace el
redeploy controlado en el servidor: **build → test → subir JAR → parar →
backup → instalar → arrancar → healthcheck**.

Se dispara manualmente:

1. En GitHub: tab **Actions → Deploy → Run workflow** (rama `main`).
2. El CI compila (`clean package`, con los 54 tests), sube el JAR por `scp`
   a `$APP_DIR/staging/` y ejecuta `sudo bash $APP_DIR/scripts/deploy.sh`.

### Secrets / variables del repositorio

Configurar en GitHub (Settings → Secrets and variables → Actions):

| Secret | Descripción |
|---|---|
| `DEPLOY_KEY` | Clave SSH privada del servidor (el runner sube como usuario no-root). |
| `DEPLOY_HOST` | IP o dominio del servidor. |
| `DEPLOY_USERNAME` | Usuario SSH (ej: `ubuntu`). |
| `DEPLOY_PORT` | Puerto SSH (default `22`; se puede omitir). |
| `DEPLOY_PATH` | Directorio de la app (default `/home/ubuntu/app`; se puede omitir). |

### Requisitos en el servidor

- Servicio systemd `lfm` operando (o ajustar `APP_SERVICE`/`APP_DIR` en
  `scripts/deploy.sh`).
- El usuario de `DEPLOY_USERNAME` debe poder usar `sudo` sin prompt para
  `systemctl`, `cp` e `install`.
- Carpeta `<APP_DIR>/staging/` con permiso de escritura para ese usuario
  (el JAR se sube ahí).
- Opcional: `<APP_DIR>/.env` con `DB_PASSWORD` para que el deploy haga backup
  previo con `scripts/backup-mysql.sh`. Sin credenciales, el backup se saltea
  y el deploy continúa.
- Subir una vez los scripts al servidor:
  `scp -r scripts/ ubuntu@ip:~/app/`

## Scripts de utilidad

- `setup-oracle-cloud.sh` — setup automático para Oracle Cloud Free Tier
- `scripts/backup-mysql.sh` — backup de MySQL con `mysqldump`, comprimido en
  `.sql.gz` y retención de 7 días (configurable con `RETENTION_DAYS`).
- `scripts/deploy.sh` — redeploy controlado en el servidor, invocado por el
  workflow de Deploy (ver sección anterior): parar → backup → instalar JAR →
  arrancar → healthcheck `/actuator/health`.
- `scripts/setup-https.sh` + `scripts/Caddyfile` — HTTPS con Caddy (Fase 4.3):
  instala el proxy y genera la config en modo HTTP `:80` o HTTPS con
  Let's Encrypt según `DOMAIN` (ver sección "HTTPS con Caddy").

### Backup automático (cron)

```bash
# Backup diario a las 03:00, guardando solo copias locales del servidor
crontab -e
```

```
0 3 * * * DB_PASSWORD='tu_password' DB_NAME=lfm /home/ubuntu/app/scripts/backup-mysql.sh >> /home/ubuntu/app/logs/backup.log 2>&1
```

Los backups quedan en `/home/ubuntu/backups/` (o en la ruta de `BACKUP_DIR`).
Se recomienda copiarlos también fuera del servidor (ej. a un bucket/DR).

## HTTPS con Caddy (Fase 4.3)

La app en `:8080` sirve `/api` y el frontend Angular se sirve por separado
(frontend en `frontend/`), así que el proxy solo agrega TLS delante. `scripts/setup-https.sh` instala Caddy y genera
`/etc/caddy/Caddyfile` en dos modos:

- **Sin `DOMAIN`**: HTTP en `:80` → `reverse_proxy 127.0.0.1:8080` (funciona hoy).
- **Con `DOMAIN`** (ej. `lfm.tudominio.com`): Let's Encrypt automático con
  renovación incluida, redirect 80→443 implícito y `scripts/Caddyfile` (bloquea
  `/actuator/*` salvo `/actuator/health`).

```bash
# Modo sin dominio (proxy base activo)
sudo bash scripts/setup-https.sh

# Flip a HTTPS cuando tengas el dominio
DOMAIN=lfm.tudominio.com ACME_EMAIL=tu@correo.com sudo bash scripts/setup-https.sh
```

Luego, en `<APP_DIR>/.env` y reiniciar la app:

```
FRONTEND_URL=https://lfm.tudominio.com
CORS_ALLOWED_ORIGINS=https://lfm.tudominio.com
```

```bash
sudo systemctl restart lfm
```

> **Fuera del script** (Oracle Cloud Console): abrir los puertos **80 y 443** en
> el security list y, una vez activo HTTPS, **cerrar el 8080 externo** para que
> todo el tráfico pase por el proxy. No requiere cambios en `deploy.sh`.

## Pendientes (TBD)

El modelo de requisitos tiene ambigüedades pendientes de validar con el cliente
(detalles en la sección "PENDIENTES TBD" de `requisitos.txt`): umbrales de Elo
por categoría, visibilidad de la contraseña del servidor, fórmula oficial de Elo,
quórum de comisarios, entre otros. Los valores por defecto actuales están
documentados en `docs/formulas-rating.md`.

## Licencia

Proyecto privado. Uso restringido.
