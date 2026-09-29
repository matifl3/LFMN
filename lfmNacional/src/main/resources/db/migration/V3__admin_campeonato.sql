-- V3: ADMIN_CAMPEONATO + campeonatos privados con lista de pilotos cerrada.
--
-- OJO: `ddl-auto=update` NO puede agregar valores a una columna ENUM de MySQL ni
-- resolver el orden de las claves foraneas nuevas, asi que esta migracion tiene
-- que correr a mano aunque Hibernate ya haya creado las columnas.
--
-- 1) ENUMs: MySQL los define en el CREATE TABLE, no en la entidad.
-- 2) Los campeonatos que ya existian quedan publicos: asi siguen apareciendo en
--    el listado historico. Su admin_id queda NULL porque no hay dueno historico.
-- 3) La tabla de miembros es la lista cerrada; sin fila no se compite.

-- ADMIN_CAMPEONATO como rol de usuario.
ALTER TABLE `usuario`
  MODIFY COLUMN `rol` enum('ADMIN','ADMIN_CAMPEONATO','COMISARIO','USUARIO')
  COLLATE utf8mb4_unicode_ci NOT NULL;

-- Aviso de sumar a un piloto a un campeonato.
ALTER TABLE `notificacion`
  MODIFY COLUMN `tipo` enum('ANUNCIO','APELACION','CAMPEONATO','CARRERA_INICIO','INCIDENTE','LOGRO','PENALIZACION','RECOMPENSA')
  COLLATE utf8mb4_unicode_ci NOT NULL;

-- Visibilidad y dueno del campeonato.
-- El DEFAULT importa: Hibernate genera `add column visibilidad enum(...) not null`
-- sin default cuando la entidad no lo declara, y MySQL le asigna a las filas
-- viejas el PRIMER literal del enum ('PRIVADO'). Con el default quedan en
-- 'PUBLICO', que es lo que corresponde a los campeonatos de la liga.
ALTER TABLE `campeonato`
  ADD COLUMN `visibilidad` enum('PRIVADO','PUBLICO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLICO',
  ADD COLUMN `admin_id` bigint DEFAULT NULL,
  ADD KEY `FK_campeonato_admin` (`admin_id`),
  ADD CONSTRAINT `FK_campeonato_admin` FOREIGN KEY (`admin_id`) REFERENCES `usuario` (`id`);

-- Red de seguridad: si la columna la creo Hibernate sin default, los
-- campeonatos historicos quedaron como PRIVADO. admin_id NULL = sin dueno, o sea
-- que no es el campeonato de un organizador, asi que se deja publico.
UPDATE `campeonato` SET `visibilidad` = 'PUBLICO' WHERE `admin_id` IS NULL;

-- Lista de pilotos por campeonato. fecha_alta es NOT NULL en la entidad, asi
-- que va en el CREATE y no como ALTER posterior.
CREATE TABLE IF NOT EXISTS `campeonato_miembro` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_alta` datetime(6) NOT NULL,
  `campeonato_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_campeonato_miembro` (`campeonato_id`,`usuario_id`),
  KEY `FK_miembro_usuario` (`usuario_id`),
  CONSTRAINT `FK_miembro_campeonato` FOREIGN KEY (`campeonato_id`) REFERENCES `campeonato` (`id`),
  CONSTRAINT `FK_miembro_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
