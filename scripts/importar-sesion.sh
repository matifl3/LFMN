#!/usr/bin/env bash
#
# Importador de sesiones de Assetto Corsa -> LFM Nacional API.
#
# Reemplaza al SesionFolderWatcher de la app: en vez de que Spring lea ./sesiones/
# del contenedor (disco efimero en Render), este script hace POST del JSON al
# endpoint idempotente. El backend responde 409 si la sesion ya fue procesada,
# aca eso significa "descartar el archivo", no error.
#
# Dependencias del host: curl, inotifywait (inotify-tools), systemd.
#
# Variables de entorno (via EnvironmentFile, ej. /home/ubuntu/app/.env):
#   API_URL             Base de la API. Default https://lfmn.onrender.com
#   TOKEN_COMISARIO    JWT de un usuario con rol COMISARIO o ADMIN (obligatorio)
#   SESION_DIR         Carpeta donde el server AC escribe los JSON
#   MAX_INTENTOS       Reintentos con backoff. Default 5
#
# CARRERA_ID se resuelve por parametro o variable, porque cambia por fecha:
#   CARRERA_ID=42 ./importar-sesion.sh
#   ./importar-sesion.sh 42

set -uo pipefail

API_URL="${API_URL:-https://lfmn.onrender.com}"
SESION_DIR="${SESION_DIR:-/home/ubuntu/app/sesiones}"
MAX_INTENTOS="${MAX_INTENTOS:-5}"
TIMEOUT="${TIMEOUT:-120}"
LOG_PREFIX="[importar-sesion]"

CARRERA_ID="${1:-${CARRERA_ID:-}}"

log() { echo "${LOG_PREFIX} $*"; }
error() { echo "${LOG_PREFIX} ERROR: $*" >&2; }

if [[ -z "${CARRERA_ID}" ]]; then
  error "falta CARRERA_ID (parametro \$1 o variable de entorno)"
  exit 1
fi

if [[ -z "${TOKEN_COMISARIO:-}" ]]; then
  error "falta TOKEN_COMISARIO"
  exit 1
fi

if [[ ! -d "${SESION_DIR}" ]]; then
  log "la carpeta ${SESION_DIR} no existe todavia; esperando"
  until [[ -d "${SESION_DIR}" ]]; do sleep 5; done
  log "carpeta ${SESION_DIR} disponible"
fi

# Crea un token de commissaire:
#   POST /api/usuarios/login  { "email": ..., "password": ... }  ->  { "token": ... }
# El token expira en 480 min (jwt.expiracion-minutos). Si el script corre mas
# tiempo que eso, hay que renovar TOKEN_COMISARIO en el .env y reiniciar la unidad.

post_sesion() {
  local archivo="$1"
  local intento=1
  local delay=5
  local http respuesta

  while (( intento <= MAX_INTENTOS )); do
    http=$(curl -s -o /tmp/lfm-sesion-resp.json -w '%{http_code}' \
      -X POST \
      -H 'Content-Type: application/json' \
      -H "Authorization: Bearer ${TOKEN_COMISARIO}" \
      --max-time "${TIMEOUT}" \
      --data-binary "@${archivo}" \
      "${API_URL}/api/sesiones/importar?carreraId=${CARRERA_ID}&nombreArchivo=$(basename "${archivo}")" 2>/dev/null)

    case "${http}" in
      200|201)
        log "OK $(basename "${archivo}") -> $(tr -d '\n' < /tmp/lfm-sesion-resp.json)"
        rm -f "${archivo}"
        return 0
        ;;
      409)
        # El backend ya conocia esta sesion: es idempotencia, no un fallo.
        log "YA PROCESADA $(basename "${archivo}")"
        rm -f "${archivo}"
        return 0
        ;;
      400|404)
        # JSON invalido o carrera inexistente: reintentar no va a ayudar.
        error "rechazado (HTTP ${http}) $(basename "${archivo}"): $(tr -d '\n' < /tmp/lfm-sesion-resp.json)"
        return 1
        ;;
      401|403)
        error "token invalido o sin permiso (HTTP ${http}). Revisar TOKEN_COMISARIO y reiniciar: systemctl restart lfm-importar-sesion"
        return 1
        ;;
      429)
        error "rate limited (HTTP 429), reintento ${intento}"
        ;;
      *)
        # 5xx y timeouts: la API esta caida o render esta despertando.
        error "HTTP ${http:-sin respuesta} en $(basename "${archivo}"), reintento ${intento}/${MAX_INTENTOS}"
        ;;
    esac

    sleep "${delay}"
    delay=$(( delay * 2 ))
    intento=$(( intento + 1 ))
  done

  error "agotados ${MAX_INTENTOS} intentos para $(basename "${archivo}"); queda en la carpeta para el proximo ciclo"
  return 1
}

log "vigilando ${SESION_DIR} -> ${API_URL} (carrera ${CARRERA_ID})"

# close_write: el archivo termino de escribirse (evita leer JSON a medio escribir).
# moved_to: cubre el caso de que el server lo renombre al terminar.
inotifywait -m -q -e close_write -e moved_to "${SESION_DIR}" --format '%f' | while read -r archivo; do
  [[ "${archivo}" == *.json ]] || continue
  post_sesion "${SESION_DIR}/${archivo}"
done