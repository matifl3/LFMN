-- Migracion de sesion_procesada.clave
--
-- Por que existe este archivo
-- --------------------------
-- La API corre con spring.jpa.hibernate.ddl-auto=update y spring.flyway.enabled=false,
-- asi que el esquema lo aplica Hibernate en cada arranque. Por eso NO se puede
-- hacer esto en un V__ de db/migration: Flyway esta apagado.
--
-- Que paso
-- --------
-- El commit 40659a32 agrego SesionProcesada.clave con @Column(nullable=false).
-- Al arrancar en prod, Hibernate genero:
--
--     ALTER TABLE sesion_procesada ADD COLUMN clave varchar(40) NOT NULL
--
-- y MySQL lo rechazo con ERROR 1364 ("Field 'clave' doesn't have a default value")
-- porque la tabla ya tenia filas de sesiones importadas. La excepcion se lanza
-- durante el arranque, el contenedor sale con status 1 y la API queda caida.
--
-- El arreglo fue volver la columna nullable en la entidad. Nullable es seguro con
-- el indice UNIQUE porque MySQL admite varios NULL en una columna UNIQUE.
--
-- Como ejecutar esto
-- -----------------
-- Correr los pasos de a uno, en orden, sobre la base de produccion.
-- Hacer backup antes: mysql dump.
--
--   mysqldump -h HOST -u USER -p lfm > lfm_backup_$(date +%F).sql
--

-- PASO 1: ver cuantas filas quedaron sin backfill.
SELECT COUNT(*) AS filas_sin_clave
FROM sesion_procesada
WHERE clave IS NULL;

-- PASO 2: backfill. Se generan claves sinteticas porque el contenido del JSON
-- original ya no esta disponible (el nombre del archivo no es la identidad real,
-- y las filas viejas se importaron por el watcher que ya no existe).
-- El prefijo 'legacy-' las marca para distinguirlas de las claves SHA-256 reales.
UPDATE sesion_procesada
SET clave = CONCAT('legacy-', id)
WHERE clave IS NULL;

-- PASO 3: verificar que no quedo ninguna. Tiene que devolver 0.
SELECT COUNT(*) ASshould_be_cero
FROM sesion_procesada
WHERE clave IS NULL;

-- PASO 4 (OPCIONAL, solo si ya no se importa nada por HTTP): apretar el
-- invariante en la base. Esto NO lo hace Hibernate con ddl-auto=update, hay que
-- correrlo a mano.
--
--   ALTER TABLE sesion_procesada MODIFY clave varchar(40) NOT NULL;
--
-- Despues de correrlo, recién ahi se puede volver a nullable=false en la entidad.
-- Si se cambia la entidad antes de este ALTER, el arranque falla otra vez.
--
-- Nota: no se puede pedir NOT NULL mientras existan filas sin clave, por el mismo
-- ERROR 1364. Por eso el orden es backfill primero, constraint despues.

-- Verificacion final del estado
SELECT COUNT(*) AS total,
       COUNT(clave) AS con_clave,
       SUM(clave LIKE 'legacy-%') AS backfilled
FROM sesion_procesada;