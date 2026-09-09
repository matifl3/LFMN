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

