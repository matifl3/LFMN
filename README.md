# LFM Nacional — Low Fuel Motorsport

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-19-red?logo=angular&logoColor=white)
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
| Frontend | Angular (build servido junto al backend) + design system estático (SCSS) |
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

> Agregar capturas de las pantallas del design system (`files/`):

- Home y próximas carreras
- Login / Registro
- Lista y detalle de carreras
- Campeonato y categorías
- Perfil de piloto y perfil propio
- Setups
- Logros y notificaciones
- Sanciones / Incidentes (panel de comisario)
- Panel de administración

## Instalación (end-to-end)

### 1. Requisitos previos

- **Java 17** (JDK)
- **Maven** (o usar el wrapper `mvnw`)
- **MySQL 8+** corriendo en `localhost:3306`
- **Node.js + npm** (solo si vas a hacer build del frontend Angular)

### 2. Crear la base de datos

```sql
CREATE DATABASE lfm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Las tablas se generan automáticamente con Hibernate (`ddl-auto=update`).

### 3. Configurar el backend

Editar `lfmNacional/src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/lfm?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=TU_USUARIO_MYSQL
spring.datasource.password=TU_PASSWORD_MYSQL

# Puerto del servidor
server.port=8080

# JWT
jwt.secreto=UNA_CLAVE_LARGA_Y_SEGURA_DE_FIRMA_HS256
jwt.expiracion-minutos=480
```

> **Recomendación de seguridad:** no commitees credenciales reales. Mové el
> password de MySQL y `jwt.secreto` a variables de entorno:
>
> ```properties
> spring.datasource.password=${DB_PASSWORD}
> jwt.secreto=${JWT_SECRETO}
> ```

Configuración de sesiones de Assetto Corsa (dónde exporta resultados el servidor):

```properties
sesiones.input-dir=D:/SteamLibrary/steamapps/common/assettocorsa/server/results
sesiones.procesadas-dir=./lfmNacional/sesiones/procesadas
sesiones.errores-dir=./lfmNacional/sesiones/errores
```

Configuración de Steam OAuth:

```properties
steam.realm=http://localhost:8080
steam.return-to=http://localhost:8080/api/steam/vinculacion/callback
steam.return-to-auth=http://localhost:8080/api/steam/auth/callback
frontend.url=http://localhost:8080
steam.state-timeout-minutos=10
```

### 4. Correr el backend

```bash
cd lfmNacional
./mvnw spring-boot:run
```

La app queda disponible en `http://localhost:8080`.

### 5. (Opcional) Build del frontend Angular

```bash
cd lfmFront
npm install
ng build
```

El build se sirve junto al backend; también hay un design system estático en
`files/` con las pantallas de referencia.

### 6. Procesar sesiones de carrera

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

## Scripts de utilidad

- `scripts/simular-carrera.ps1` — simula el flujo de una carrera para pruebas
  (inscripción, resultados y recálculo de rating).

## Pendientes (TBD)

El modelo de requisitos tiene ambigüedades pendientes de validar con el cliente
(detalles en la sección "PENDIENTES TBD" de `requisitos.txt`): umbrales de Elo
por categoría, visibilidad de la contraseña del servidor, fórmula oficial de Elo,
quórum de comisarios, entre otros. Los valores por defecto actuales están
documentados en `docs/formulas-rating.md`.

## Licencia

Proyecto privado. Uso restringido.
