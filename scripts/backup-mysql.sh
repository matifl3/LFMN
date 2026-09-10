#!/usr/bin/env bash
#
# LFM Nacional - Backup de MySQL
# Dependencias: mysqldump (cliente MySQL), gzip
#
# Uso:
#   DB_PASSWORD=... ./scripts/backup-mysql.sh
#
# Variables de entorno (obligatorias):
#   DB_PASSWORD   Password de la BD
#   DB_NAME       Nombre de la base (default: lfm)
#   DB_HOST       Host (default: localhost)
#   DB_USER       Usuario (default: lfm_user)
#   BACKUP_DIR    Directorio destino (default: /home/ubuntu/backups)
#   RETENTION_DAYS Dias a conservar (default: 7)
#
# Cron sugerido (todos los dias 03:00):
#   0 3 * * * DB_PASSWORD='...' DB_NAME=lfm /home/ubuntu/app/scripts/backup-mysql.sh >> /home/ubuntu/app/logs/backup.log 2>&1

set -euo pipefail

DB_NAME="${DB_NAME:-lfm}"
DB_HOST="${DB_HOST:-localhost}"
DB_USER="${DB_USER:-lfm_user}"
BACKUP_DIR="${BACKUP_DIR:-/home/ubuntu/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "ERROR: DB_PASSWORD no definida" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

STAMP="$(date +%Y%m%d_%H%M%S)"
FILE="$BACKUP_DIR/${DB_NAME}_${STAMP}.sql.gz"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Iniciando backup de $DB_NAME en $FILE"

mysqldump \
  --host="$DB_HOST" \
  --user="$DB_USER" \
  "--password=$DB_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  --databases "$DB_NAME" \
  | gzip > "$FILE"

# Retencion: borrar backups mas viejos que RETENTION_DAYS
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime "+$((RETENTION_DAYS - 1))" -delete

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Backup OK: $FILE"