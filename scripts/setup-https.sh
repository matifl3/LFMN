#!/usr/bin/env bash
#
# LFM Nacional - HTTPS con Caddy (Fase 4.3)
# Instala Caddy, genera /etc/caddy/Caddyfile y abre el firewall 80/443.
#
# Dos modos segun DOMAIN:
#   - DOMAIN definido  -> HTTPS automatico con Let's Encrypt (usa scripts/Caddyfile)
#   - DOMAIN vacio     -> HTTP en :80 por detras del proxy (listo para el flip)
#
# Variables de entorno (opcionales):
#   DOMAIN      Dominio publico apuntando a la IP del VM (ej: lfm.tudominio.com)
#   ACME_EMAIL  Email para Let's Encrypt (recomendado, para avisos de expiracion)
#
# Uso:
#   sudo DOMAIN=lfm.tudominio.com ACME_EMAIL=tucorreo@example.com bash scripts/setup-https.sh
#
# Fuera de este script (Oracle Cloud Console): abrir los puertos 80 y 443 en el
# security list y, una vez activo HTTPS, cerrar el 8080 externo para que todo el
# trafico pase por el proxy.

set -euo pipefail

DOMAIN="${DOMAIN:-}"
ACME_EMAIL="${ACME_EMAIL:-}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE="$SCRIPT_DIR/Caddyfile"
CADDY_CONFIG="/etc/caddy/Caddyfile"
CFG_TMP="$(mktemp /tmp/caddy.Caddyfile.XXXXXX)"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

cleanup() { rm -f "$CFG_TMP"; }
trap cleanup EXIT

if [[ "$(id -u)" -ne 0 ]] && ! sudo -n true 2>/dev/null; then
  echo "ERROR: se necesita sudo sin prompt (o ejecutalo como root)" >&2
  exit 1
fi

# 1) Instalar Caddy desde el repo oficial si no esta
if ! command -v caddy >/dev/null 2>&1; then
  log "Instalando Caddy desde el repo oficial..."
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://dl.cloudsmith.io/public/caddy/stable/gpg.key \
    | sudo gpg --dearmor -o /etc/apt/keyrings/caddy-stable-archive-keyring.gpg
  curl -fsSL https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt \
    | sudo tee /etc/apt/sources.list.d/caddy-stable.list >/dev/null
  sudo apt-get update
  sudo apt-get install -y caddy
  log "Caddy instalado: $(caddy version)"
else
  log "Caddy ya instalado: $(caddy version)"
fi

# 2) Generar la configuracion
if [[ -n "$DOMAIN" ]]; then
  if [[ ! "$DOMAIN" =~ ^[A-Za-z0-9.-]+\.[A-Za-z]{2,}$ ]]; then
    echo "ERROR: DOMAIN no parece un nombre de dominio valido: $DOMAIN" >&2
    exit 1
  fi
  log "Modo HTTPS para $DOMAIN"
  sed -e 's/{\$DOMAIN}/'"$DOMAIN"'/g' \
      -e 's/{\$ACME_EMAIL}/'"$ACME_EMAIL"'/g' \
      "$TEMPLATE" > "$CFG_TMP"
  # Si no hay ACME_EMAIL, quitar la directiva "email" vacia
  sed -i -E '/^[[:space:]]*email[[:space:]]*$/d' "$CFG_TMP"
else
  log "Modo sin dominio: HTTP en :80 (HTTPS se activa al definir DOMAIN)"
  cat > "$CFG_TMP" <<'EOCADDY'
# LFM Nacional - Caddy (modo sin dominio): HTTP :80 -> app :8080.
# Flip a HTTPS cuando exista el dominio:
#   DOMAIN=lfm.tudominio.com sudo bash scripts/setup-https.sh
:80 {
	encode gzip
	reverse_proxy 127.0.0.1:8080
}
EOCADDY
fi

# 3) Validar antes de aplicar
sudo caddy validate --config "$CFG_TMP" >/dev/null

# 4) Aplicar (respaldando la config previa)
sudo cp "$CADDY_CONFIG" "$CADDY_CONFIG.bak" 2>/dev/null || true
sudo install -m 0644 "$CFG_TMP" "$CADDY_CONFIG"

# 5) Abrir firewall
if command -v ufw >/dev/null 2>&1 && sudo ufw status | grep -q 'Status: active'; then
  sudo ufw allow 80/tcp >/dev/null
  sudo ufw allow 443/tcp >/dev/null
  log "ufw: abiertos 80 y 443"
fi

# 6) Habilitar y arrancar
sudo systemctl enable --now caddy
sudo systemctl restart caddy
log "caddy: activo"

# 7) Verificacion (best effort: requiere la app arriba)
URL="${DOMAIN:+https://$DOMAIN}"; URL="${URL:-http://localhost}"
if curl -fsS --max-time 10 "${URL}/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
  log "OK: $URL responde healthy."
else
  log "WARN: no se confirmo /actuator/health en $URL (¿la app esta corriendo?)."
fi

log "Recordatorio (fuera de este script):"
if [[ -n "$DOMAIN" ]]; then
  log "  - En Oracle Cloud Console abrir 80 y 443 en el security list."
  log "  - Cerrar el 8080 externo para obligar trafico por el proxy."
  log "  - En <APP_DIR>/.env: FRONTEND_URL=https://$DOMAIN y CORS_ALLOWED_ORIGINS=https://$DOMAIN; luego reiniciar la app."
else
  log "  - Configurar el dominio en Oracle/security list cuando exista, y relanzar:"
  log "      DOMAIN=<dominio> sudo bash scripts/setup-https.sh"
fi