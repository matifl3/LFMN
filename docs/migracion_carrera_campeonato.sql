-- Migracion: Carrera pasa de pertenecer directamente a Categoria a pertenecer a Campeonato
-- Ejecutar ANTES de levantar la aplicacion con los nuevos cambios
-- Base de datos: lfm (MySQL)

-- 1. Agregar columna campeonato_id a la tabla carrera
ALTER TABLE carrera ADD COLUMN campeonato_id BIGINT NULL AFTER categoria_id;

-- 2. Migrar datos existentes: mapear cada carrera al primer campeonato activo de su categoria
--    Si hay varios campeonatos activos, se toma el de menor id
UPDATE carrera c
INNER JOIN campeonato cam ON cam.categoria_id = c.categoria_id AND cam.estado = 'ACTIVO'
SET c.campeonato_id = cam.id
WHERE c.campeonato_id IS NULL;

-- 3. Si no hay campeonatos activos, mapear al primer campeonato de la categoria (cualquier estado)
UPDATE carrera c
INNER JOIN campeonato cam ON cam.categoria_id = c.categoria_id
SET c.campeonato_id = cam.id
WHERE c.campeonato_id IS NULL
ORDER BY cam.id ASC;

-- 4. Verificar que no queden carreras sin campeonato asignado
-- Si alguna queda sin asignar, revisar manualmente los datos
SELECT c.id, c.nombre, c.categoria_id, c.campeonato_id
FROM carrera c
WHERE c.campeonato_id IS NULL;

-- 5. Hacer NOT NULL la columna y agregar constraint FK
ALTER TABLE carrera MODIFY COLUMN campeonato_id BIGINT NOT NULL;
ALTER TABLE carrera ADD CONSTRAINT fk_carrera_campeonato
    FOREIGN KEY (campeonato_id) REFERENCES campeonato(id);

-- 6. Eliminar la columna categoria_id (ya no se usa directamente)
--    La categoria se obtiene via: carrera -> campeonato -> categoria
ALTER TABLE carrera DROP FOREIGN KEY fk_carrera_categoria;
ALTER TABLE carrera DROP COLUMN categoria_id;
