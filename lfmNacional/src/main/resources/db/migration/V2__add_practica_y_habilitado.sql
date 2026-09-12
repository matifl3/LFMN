-- V2: Agrega practica_fecha (RF-034) y habilitado (RF-066)
ALTER TABLE `carrera` ADD COLUMN `practica_fecha` datetime(6) DEFAULT NULL;
ALTER TABLE `usuario` ADD COLUMN `habilitado` bit(1) NOT NULL DEFAULT b'1';