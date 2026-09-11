-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lfm_flyway_v1
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `anuncio`
--

DROP TABLE IF EXISTS `anuncio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anuncio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contenido` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `destacado` bit(1) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `titulo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `url_imagen` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `apelacion`
--

DROP TABLE IF EXISTS `apelacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `apelacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('APROBADA','PENDIENTE','RECHAZADA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `respuesta_admin` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sancion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcsrg3wfg3h9b02188xhrlhpax` (`sancion_id`),
  KEY `FKs1w9xkmoi43tesbsfhg8thqsr` (`usuario_id`),
  CONSTRAINT `FKcsrg3wfg3h9b02188xhrlhpax` FOREIGN KEY (`sancion_id`) REFERENCES `sancion` (`id`),
  CONSTRAINT `FKs1w9xkmoi43tesbsfhg8thqsr` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archivo_carrera`
--

DROP TABLE IF EXISTS `archivo_carrera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `archivo_carrera` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ruta` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` enum('CARGA','OTRO','PAQUETE','SETUP') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `campeonato`
--

DROP TABLE IF EXISTS `campeonato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campeonato` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('ACTIVO','CERRADO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sistema_puntos` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `temporada` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `categoria_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7a9uc2vjjtssdgl1inynpt23n` (`categoria_id`),
  CONSTRAINT `FK7a9uc2vjjtssdgl1inynpt23n` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `campeonato_posicion`
--

DROP TABLE IF EXISTS `campeonato_posicion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campeonato_posicion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `posicion` int NOT NULL,
  `puntos` int NOT NULL,
  `campeonato_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt1drl8fft6nin60o2ftyn0038` (`campeonato_id`,`usuario_id`),
  KEY `FKjm4rdgwcbkn4i420jjvo0nrt` (`usuario_id`),
  CONSTRAINT `FKd65nm355vem4srxavu99qpekd` FOREIGN KEY (`campeonato_id`) REFERENCES `campeonato` (`id`),
  CONSTRAINT `FKjm4rdgwcbkn4i420jjvo0nrt` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carrera`
--

DROP TABLE IF EXISTS `carrera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrera` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `circuito` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contrasena_servidor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cupo_maximo` int DEFAULT NULL,
  `estado` enum('CANCELADA','EN_CURSO','FINALIZADA','INSCRIPCIONES_ABIERTAS','INSCRIPCIONES_CERRADAS','PROGRAMADA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `link_auto` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_pista` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `servidor` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `archivo_id` bigint DEFAULT NULL,
  `campeonato_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_carrera_estado_fecha` (`estado`,`fecha`),
  KEY `idx_carrera_campeonato_fecha` (`campeonato_id`,`fecha`),
  KEY `FK5ji16436cugn74xx5sbk8f8c0` (`archivo_id`),
  CONSTRAINT `FK5ji16436cugn74xx5sbk8f8c0` FOREIGN KEY (`archivo_id`) REFERENCES `archivo_carrera` (`id`),
  CONSTRAINT `FK93qfseeoni0gj0wbp71o0d5fu` FOREIGN KEY (`campeonato_id`) REFERENCES `campeonato` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `elo_maximo` int DEFAULT NULL,
  `elo_minimo` int DEFAULT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `setup_abierto` bit(1) DEFAULT NULL,
  `setup_fijo` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `elo_sancion`
--

DROP TABLE IF EXISTS `elo_sancion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `elo_sancion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cambio` int NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `carrera_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_elo_usuario_fecha` (`usuario_id`,`fecha`),
  KEY `idx_elo_carrera` (`carrera_id`),
  CONSTRAINT `FK4in16h9rfk0jndmd5381mndyh` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKg6js45jjydd0dbkx4lxh0dgnc` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidente`
--

DROP TABLE IF EXISTS `incidente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` enum('EN_ANALISIS','PENDIENTE','RESUELTO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `video_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `vuelta` int DEFAULT NULL,
  `carrera_id` bigint NOT NULL,
  `reportante_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_incidente_estado_carrera` (`estado`,`carrera_id`),
  KEY `FKprnqmy2weug0jr189n8u74dbw` (`carrera_id`),
  KEY `FKbfie5w2d9w6680k9t2bhtdt8u` (`reportante_id`),
  CONSTRAINT `FKbfie5w2d9w6680k9t2bhtdt8u` FOREIGN KEY (`reportante_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKprnqmy2weug0jr189n8u74dbw` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `incidente_piloto`
--

DROP TABLE IF EXISTS `incidente_piloto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `incidente_piloto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rol` enum('AFECTADO','CAUSANTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `incidente_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4npvt5b2dx1muj2nwiyq559ef` (`incidente_id`,`usuario_id`),
  KEY `FKfwh3w1w1n9ncyh2hlo3i7lcj0` (`usuario_id`),
  CONSTRAINT `FK1dulvafjjxdqehd8hggtw63k7` FOREIGN KEY (`incidente_id`) REFERENCES `incidente` (`id`),
  CONSTRAINT `FKfwh3w1w1n9ncyh2hlo3i7lcj0` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inscripcion`
--

DROP TABLE IF EXISTS `inscripcion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inscripcion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('CANCELADA','INSCRIPTO','LISTA_ESPERA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_inscripcion` datetime(6) NOT NULL,
  `carrera_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKayin6w68l7alp2587qxbhgbdy` (`carrera_id`,`usuario_id`),
  KEY `idx_ins_carrera_estado` (`carrera_id`,`estado`),
  KEY `idx_ins_usuario` (`usuario_id`),
  CONSTRAINT `FK9p0vjmtxjgjp0wbdmcnik7vj5` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`),
  CONSTRAINT `FKlm8x8v2liyy6jg0fyu93w4lv8` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logro`
--

DROP TABLE IF EXISTS `logro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logro` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icono` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo_condicion` enum('CARRERAS','CARRERAS_COMPLETADAS','ELO','PODIOS','POLES','VICTORIAS','VUELTAS_RAPIDAS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor_condicion` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) NOT NULL,
  `leida` bit(1) NOT NULL,
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mensaje` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` enum('ANUNCIO','APELACION','CARRERA_INICIO','INCIDENTE','LOGRO','PENALIZACION','RECOMPENSA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notif_usuario_leida_fecha` (`usuario_id`,`leida`,`fecha`),
  CONSTRAINT `FK5hnclv9lmmc1w4335x04warbm` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recompensa`
--

DROP TABLE IF EXISTS `recompensa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recompensa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` enum('DESCUENTO','FISICA','OTRA','VIRTUAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `logro_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb1hunx7cb3e9edhj12cdbbuyi` (`logro_id`),
  CONSTRAINT `FKb1hunx7cb3e9edhj12cdbbuyi` FOREIGN KEY (`logro_id`) REFERENCES `logro` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resolucion_incidente`
--

DROP TABLE IF EXISTS `resolucion_incidente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resolucion_incidente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `explicacion` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `comisario_id` bigint NOT NULL,
  `incidente_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhxhu3h390y91o03s7v20nm61y` (`incidente_id`),
  KEY `FKklg33lk8yq32nd5qxghjpnejb` (`comisario_id`),
  CONSTRAINT `FKklg33lk8yq32nd5qxghjpnejb` FOREIGN KEY (`comisario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKs41ev0vbcgq8px6qqfxhemqh` FOREIGN KEY (`incidente_id`) REFERENCES `incidente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resultado_carrera`
--

DROP TABLE IF EXISTS `resultado_carrera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resultado_carrera` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elo_ganado` int DEFAULT NULL,
  `finalizo` bit(1) NOT NULL,
  `modelo_auto` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `poles` bit(1) NOT NULL,
  `posicion_final` int DEFAULT NULL,
  `skin_auto` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sr_ganado` int DEFAULT NULL,
  `tiempo_total` bigint DEFAULT NULL,
  `vuelta_rapida` bigint DEFAULT NULL,
  `carrera_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9k5lj2ne01uqdub5c31lu30k3` (`carrera_id`,`usuario_id`),
  KEY `idx_rc_usuario` (`usuario_id`),
  KEY `idx_rc_usuario_finalizo` (`usuario_id`,`finalizo`),
  KEY `idx_rc_usuario_posicion` (`usuario_id`,`posicion_final`),
  KEY `idx_rc_carrera_vuelta_rapida` (`carrera_id`,`vuelta_rapida`),
  CONSTRAINT `FK1ubrdc87g7qs15q1npq7gagyr` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK8tsinnaxdqlp9p3vapgros9tn` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `safety_rating_sancion`
--

DROP TABLE IF EXISTS `safety_rating_sancion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `safety_rating_sancion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cambio` int NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `carrera_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sr_usuario_fecha` (`usuario_id`,`fecha`),
  KEY `idx_sr_carrera` (`carrera_id`),
  CONSTRAINT `FKbxrluy0kp6rgmhmo49u9d3qw2` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`),
  CONSTRAINT `FKovomiop7683by0541qak1i86e` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sancion`
--

DROP TABLE IF EXISTS `sancion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sancion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `efectos_aplicados` bit(1) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `id_externo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `origen` enum('ADMIN','COMISARIO','REAL_PENALTY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` enum('DESCALIFICACION','DRIVE_THROUGH','ELO','PUESTOS','SAFETY_RATING','SEGUNDOS','STOP_AND_GO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor` int DEFAULT NULL,
  `carrera_id` bigint DEFAULT NULL,
  `resolucion_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sancion_usuario_fecha` (`usuario_id`,`fecha`),
  KEY `idx_sancion_origen_id_externo` (`origen`,`id_externo`),
  KEY `FKrhxvifl1ivtqsg5hbcirf1u4x` (`carrera_id`),
  KEY `FKa57mvx4lpbttn6jlnj7gcr5b2` (`resolucion_id`),
  CONSTRAINT `FKa57mvx4lpbttn6jlnj7gcr5b2` FOREIGN KEY (`resolucion_id`) REFERENCES `resolucion_incidente` (`id`),
  CONSTRAINT `FKp8b86widtt3e44nxvok9upjwo` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKrhxvifl1ivtqsg5hbcirf1u4x` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sesion_clasificacion`
--

DROP TABLE IF EXISTS `sesion_clasificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sesion_clasificacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `diferencia_pole` bigint DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  `modelo_auto` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `skin_auto` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tiempo` bigint NOT NULL,
  `carrera_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8c3e0a3e0is9atoh76uqt33t0` (`carrera_id`),
  KEY `FKjf3pr0a1p0loqqfk07xh978fp` (`usuario_id`),
  CONSTRAINT `FK8c3e0a3e0is9atoh76uqt33t0` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`),
  CONSTRAINT `FKjf3pr0a1p0loqqfk07xh978fp` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sesion_procesada`
--

DROP TABLE IF EXISTS `sesion_procesada`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sesion_procesada` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_procesamiento` datetime(6) NOT NULL,
  `nombre_archivo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `carrera_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbrxbge25viyflnnim1x0h0wag` (`nombre_archivo`),
  KEY `FK8o6hdactic9j8kv4wmcvfk6mf` (`carrera_id`),
  CONSTRAINT `FK8o6hdactic9j8kv4wmcvfk6mf` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `setup`
--

DROP TABLE IF EXISTS `setup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `setup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `circuito` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_publicacion` datetime(6) NOT NULL,
  `promedio_calificacion` double DEFAULT NULL,
  `titulo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehiculo` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `autor_id` bigint NOT NULL,
  `categoria_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_setup_autor` (`autor_id`),
  KEY `idx_setup_categoria` (`categoria_id`),
  CONSTRAINT `FK13istv2w2yr44q7cxdox0hly8` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`),
  CONSTRAINT `FKj0h1m46vic7wkrcdb3k3fwcb1` FOREIGN KEY (`autor_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `setup_calificacion`
--

DROP TABLE IF EXISTS `setup_calificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `setup_calificacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `puntaje` int NOT NULL,
  `setup_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5c711dbpgremyq1nbgb8iinmk` (`setup_id`,`usuario_id`),
  KEY `FKftckgrurx9wk5wx89bs5jdhhm` (`usuario_id`),
  CONSTRAINT `FKcp2qhghvqjgtm5mamplgbot3k` FOREIGN KEY (`setup_id`) REFERENCES `setup` (`id`),
  CONSTRAINT `FKftckgrurx9wk5wx89bs5jdhhm` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `setup_comentario`
--

DROP TABLE IF EXISTS `setup_comentario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `setup_comentario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) NOT NULL,
  `texto` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `setup_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2dlswq79uheaucyc4g4wyc46g` (`setup_id`),
  KEY `FKbf425l55bnihgcpcu6d62yv6u` (`usuario_id`),
  CONSTRAINT `FK2dlswq79uheaucyc4g4wyc46g` FOREIGN KEY (`setup_id`) REFERENCES `setup` (`id`),
  CONSTRAINT `FKbf425l55bnihgcpcu6d62yv6u` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elo` int NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `foto_perfil` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `guid_steam` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nombre_piloto` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_establecida` tinyint(1) NOT NULL DEFAULT '1',
  `rol` enum('ADMIN','COMISARIO','USUARIO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `safety_rating` int NOT NULL,
  `token_version` int NOT NULL DEFAULT '0',
  `version` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5171l57faosmj8myawaucatdw` (`email`),
  UNIQUE KEY `UKjgyabtgdps4j4bkokgby7fti6` (`guid_steam`),
  UNIQUE KEY `UKk0o1wj2a7pseuq4icky93yc26` (`nombre_piloto`),
  CONSTRAINT `usuario_chk_1` CHECK ((`elo` >= 0)),
  CONSTRAINT `usuario_chk_2` CHECK ((`safety_rating` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario_logro`
--

DROP TABLE IF EXISTS `usuario_logro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_logro` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_obtencion` datetime(6) DEFAULT NULL,
  `obtenido` bit(1) NOT NULL,
  `progreso` int NOT NULL,
  `logro_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3puqbo6ec80r2r2mdvnoqopg6` (`logro_id`,`usuario_id`),
  KEY `idx_ul_usuario_obtenido` (`usuario_id`,`obtenido`),
  CONSTRAINT `FK6631lpk26mro1r8xk6oarnbr` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKep9076llnkpe8xyauw6vr5684` FOREIGN KEY (`logro_id`) REFERENCES `logro` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario_recompensa`
--

DROP TABLE IF EXISTS `usuario_recompensa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_recompensa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) NOT NULL,
  `reclamada` bit(1) NOT NULL,
  `recompensa_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnn5seoqs2c48ieyr2ro8jo6rr` (`recompensa_id`,`usuario_id`),
  KEY `FKscjjvtwspld8ped6d2qw9e1is` (`usuario_id`),
  CONSTRAINT `FKoyiaahl0lfwmdad8i15xa1w2r` FOREIGN KEY (`recompensa_id`) REFERENCES `recompensa` (`id`),
  CONSTRAINT `FKscjjvtwspld8ped6d2qw9e1is` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `voto_comisario`
--

DROP TABLE IF EXISTS `voto_comisario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voto_comisario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comentario` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision` enum('ABSTENCION','A_FAVOR','EN_CONTRA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `comisario_id` bigint NOT NULL,
  `incidente_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7t05tubmxsiap75qomqt8vy8p` (`incidente_id`,`comisario_id`),
  KEY `FKltkxlhmvgq73c3hsylre2bn0u` (`comisario_id`),
  CONSTRAINT `FK3vbranh3ckg5st169yq2yfgun` FOREIGN KEY (`incidente_id`) REFERENCES `incidente` (`id`),
  CONSTRAINT `FKltkxlhmvgq73c3hsylre2bn0u` FOREIGN KEY (`comisario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vuelta_carrera`
--

DROP TABLE IF EXISTS `vuelta_carrera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vuelta_carrera` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cortes` int DEFAULT NULL,
  `neumatico` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero_vuelta` int NOT NULL,
  `sector1` bigint DEFAULT NULL,
  `sector2` bigint DEFAULT NULL,
  `sector3` bigint DEFAULT NULL,
  `tiempo_ms` bigint NOT NULL,
  `tipo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `carrera_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm84w9vrng7uerfi292t2y8tmg` (`carrera_id`,`usuario_id`,`numero_vuelta`,`tipo`),
  KEY `idx_vc_carrera_usuario` (`carrera_id`,`usuario_id`),
  KEY `idx_vc_carrera_tipo` (`carrera_id`,`tipo`),
  KEY `FKk1ak9855jdv6s2hr0bn56buor` (`usuario_id`),
  CONSTRAINT `FK9mvpnjyvd9niqat746q3s3ok4` FOREIGN KEY (`carrera_id`) REFERENCES `carrera` (`id`),
  CONSTRAINT `FKk1ak9855jdv6s2hr0bn56buor` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'lfm_flyway_v1'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09 19:39:45
