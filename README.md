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
| Frontend | HTML/CSS/JS estático (`files/`) con design system SCSS |
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

> Capturas de las pantallas del frontend (`files/`):

- Home y próximas carreras
- Login / Registro
- Lista y detalle de carreras
- Campeonato y categorías
- Perfil de piloto y perfil propio
- Setups
- Logros y notificaciones
- Sanciones / Incidentes (panel de comisario)
- Panel de administración

## Instalación (desarrollo local)

### 1. Requisitos previos

- **Java 17** (JDK)
- **Maven** (o usar el wrapper `mvnw`)
- **MySQL 8+** corriendo en `localhost:3306`

### 2. Crear la base de datos

```sql
CREATE DATABASE lfm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Las tablas se generan automáticamente con Hibernate (`ddl-auto=update`).

### 3. Configurar variables de entorno

Copiar el archivo de ejemplo y completar los valores:

```bash
cd lfmNacional
cp .env.example .env
```

Editar `.env` con tus valores:

```properties
DB_USERNAME=root
DB_PASSWORD=tu_password_mysql
JWT_SECRETO=clave_larga_aleatoria_para_firmar_tokens
FRONTEND_URL=http://localhost:8080
CORS_ALLOWED_ORIGINS=*
```

> **Seguridad**: el archivo `.env` está en `.gitignore` y nunca se sube a git.
> En desarrollo local, `application.properties` tiene un default para `JWT_SECRETO`
> pero es recomendable crear `.env` con valores seguros.

### 4. Configurar sesiones de Assetto Corsa

En `.env`, configurar la ruta donde el servidor de Assetto Corsa exporta resultados:

```properties
SESIONES_DIR=/ruta/a/tus/sesiones
```

O dejar el valor por defecto (funciona en desarrollo local).

### 5. Correr el backend

```bash
cd lfmNacional
./mvnw spring-boot:run
```

La app queda disponible en `http://localhost:8080`.

### 6. Abrir el frontend

Abrí `http://localhost:8080` en tu navegador. El frontend es servido automáticamente
por el backend desde la carpeta `files/`.

### 7. Procesar sesiones de carrera

El watcher de carpeta detecta automáticamente los JSON exportados por Assetto
Corsa con el formato:

```
<carreraId>_<fecha>_<TIPO>.json        ej: 1_2026_8_7_16_31_RACE.json
```

También podés importar una sesión manualmente:

```bash
curl -X POST "http://localhost:8080/api/sesiones/importar?carreraId=1" \
  -H "Content-Type: application/json" \
  -d @sesion.json
```

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
scp -r files/ ubuntu@ip:~/app/

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

### Perfiles de Spring

- **default** (desarrollo): `ddl-auto=update`, `show-sql=true`, datos de test
- **prod** (producción): `ddl-auto=validate`, `show-sql=false`, sin DataSeeder

## Scripts de utilidad

- `setup-oracle-cloud.sh` — setup automático para Oracle Cloud Free Tier

## Pendientes (TBD)

El modelo de requisitos tiene ambigüedades pendientes de validar con el cliente
(detalles en la sección "PENDIENTES TBD" de `requisitos.txt`): umbrales de Elo
por categoría, visibilidad de la contraseña del servidor, fórmula oficial de Elo,
quórum de comisarios, entre otros. Los valores por defecto actuales están
documentados en `docs/formulas-rating.md`.

## Licencia

Proyecto privado. Uso restringido.
