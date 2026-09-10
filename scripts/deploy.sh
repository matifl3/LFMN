#!/usr/bin/env bash
#
# LFM Nacional - Deploy automatizado (Fase 4.2)
# Redeploy controlado: parar -> backup -> instalar JAR -> arrancar -> healthcheck.
# Se ejecuta en el servidor, normalmente disparado por el workflow deploy.yml
# de GitHub Actions cuando el JAR llega a "$APP_DIR/staging/".
#
# Requisitos del servidor:
#   - Servicio systemd "$APP_SERVICE" (default: lfm), usuario con sudo sin prompt.
#   - JAR subido por el CI en "$STAGING_DIR" con el nombre $STAGED_JAR.
#   - Opcional: "$ENV_FILE" con credenciales (DB_PASSWORD, etc.) para el backup.
#
# Configuracion por variables de entorno (todas opcionales):
#   APP_SERVICE      Nombre del servicio systemd (default: lfm)
#   APP_DIR          Directorio de la app (default: /home/ubuntu/app)
#   STAGING_DIR      Home del JAR nuevo (default: $APP_DIR/staging)
#   ENV_FILE         Archivo de entorno del server (default: $APP_DIR/.env)
#   BACKUP_SCRIPT    Script de backup (default: $APP_DIR/scripts/backup-mysql.sh)
#   HEALTH_URL       Health check (default: http://localhost:8080/actuator/health)
#   HEALTH_TIMEOUT_SEC Tiempo maximo esperando healthy (default: 120)
#   STAGED_JAR       Nombre del jar en staging (default: lfmNacional-0.0.1-SNAPSHOT.jar)
#   DEPLOYED_JAR     Destino del jar (default: $APP_DIR/app.jar)

set -euo pipefail

APP_SERVICE="${APP_SERVICE:-lfm}"
APP_DIR="${APP_DIR:-/home/ubuntu/app}"
STAGING_DIR="${STAGING_DIR:-$APP_DIR/staging}"
ENV_FILE="${ENV_FILE:-$APP_DIR/.env}"
BACKUP_SCRIPT="${BACKUP_SCRIPT:-$APP_DIR/scripts/backup-mysql.sh}"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/actuator/health}"
HEALTH_TIMEOUT_SEC="${HEALTH_TIMEOUT_SEC:-120}"
STAGED_JAR="${STAGED_JAR:-$STAGING_DIR/lfmNacional-0.0.1-SNAPSHOT.jar}"
DEPLOYED_JAR="${DEPLOYED_JAR:-$APP_DIR/app.jar}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

mkdir -p "$APP_DIR" "$STAGING_DIR"

if [[ ! -f "$STAGED_JAR" ]]; then
  log "ERROR: no se encuentra $STAGED_JAR"
  exit 1
fi

log "=== Deploy comienza ==="
log "Servicio: $APP_SERVICE | App: $APP_DIR"

# 1) Parar la app
log "1/5 Parando $APP_SERVICE..."
sudo systemctl stop "$APP_SERVICE"

# 2) Backup de la BD (best effort: si no hay credenciales o el script no existe, continua)
log "2/5 Backup de la BD..."
if [[ -f "$BACKUP_SCRIPT" ]]; then
  if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    set +a
  fi
  if [[ -n "${DB_PASSWORD:-}" ]]; then
    bash "$BACKUP_SCRIPT" || log "WARN: el backup fallo, el deploy continua"
  else
    log "WARN: sin DB_PASSWORD en $ENV_FILE, se saltea el backup"
  fi
else
  log "WARN: no existe $BACKUP_SCRIPT, se saltea el backup"
fi

# 3) Instalar el JAR nuevo (mantener el anterior como .bak)
log "3/5 Instalando JAR..."
sudo cp "$DEPLOYED_JAR" "$DEPLOYED_JAR.bak" 2>/dev/null || true
sudo install -m 0644 "$STAGED_JAR" "$DEPLOYED_JAR"
sudo rm -f "$STAGED_JAR"

# 4) Arrancar
log "4/5 Arrancando $APP_SERVICE..."
sudo systemctl start "$APP_SERVICE"

# 5) Healthcheck
log "5/5 Esperando health en $HEALTH_URL (max ${HEALTH_TIMEOUT_SEC}s)..."
deadline=$((SECONDS + HEALTH_TIMEOUT_SEC))
while (( SECONDS < deadline )); do
  if curl -fsS --max-time 5 "$HEALTH_URL" 2>/dev/null | grep -q '"status":"UP"'; then
    log "Deploy OK: $APP_SERVICE arribo."
    exit 0
  fi
  sleep 3
done

log "ERROR: $APP_SERVICE no respondio healthy en ${HEALTH_TIMEOUT_SEC}s"
sudo systemctl status "$APP_SERVICE" --no-pager 2>/dev/null | tail -n 20 || true
exit 1