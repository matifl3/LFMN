#!/bin/bash
# ============================================
# LFM Nacional - Script de Setup para Oracle Cloud
# ============================================
# Ejecutar como root en la VM de Oracle Cloud
# Uso: sudo bash setup-oracle-cloud.sh

set -e

echo "=== LFM Nacional - Setup Oracle Cloud Free Tier ==="
echo ""

# ---- Variables ----
APP_USER="ubuntu"
APP_DIR="/home/$APP_USER/app"
DB_NAME="lfm"
DB_USER="lfm_user"
DB_PASS="${DB_PASSWORD:-$(openssl rand -base64 16)}"
JWT_SECRET=$(openssl rand -base64 64)
FRONTEND_URL="${FRONTEND_URL:-http://$(curl -s ifconfig.me):8080}"

echo "1. Actualizando sistema..."
apt update && apt upgrade -y

echo "2. Instalando Java 17..."
apt install openjdk-17-jdk -y

echo "3. Instalando MySQL 8..."
apt install mysql-server -y
systemctl enable mysql
systemctl start mysql

echo "4. Configurando MySQL..."
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF

echo "5. Creando estructura de directorios..."
mkdir -p $APP_DIR
mkdir -p $APP_DIR/sesiones/procesadas
mkdir -p $APP_DIR/sesiones/errores
mkdir -p $APP_DIR/archivos
chown -R $APP_USER:$APP_USER $APP_DIR

echo "6. Creando archivo .env..."
cat > $APP_DIR/.env <<EOF
# LFM Nacional - Variables de Entorno
DB_USERNAME=$DB_USER
DB_PASSWORD=$DB_PASS
JWT_SECRETO=$JWT_SECRET
FRONTEND_URL=$FRONTEND_URL
CORS_ALLOWED_ORIGINS=$FRONTEND_URL
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF
chown $APP_USER:$APP_USER $APP_DIR/.env
chmod 600 $APP_DIR/.env

echo "7. Creando servicio systemd..."
cat > /etc/systemd/system/lfm.service <<EOF
[Unit]
Description=LFM Nacional - Sim Racing League
After=network.target mysql.service

[Service]
User=$APP_USER
WorkingDirectory=$APP_DIR
EnvironmentFile=$APP_DIR/.env
ExecStart=/usr/bin/java -jar $APP_DIR/app.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable lfm

echo "8. Configurando firewall..."
# Oracle Cloud usa iptables internamente
# Abrir puerto 8080
if command -v ufw &> /dev/null; then
    ufw allow 8080/tcp
    ufw reload
fi

echo ""
echo "=== Setup completado ==="
echo ""
echo "ARCHIVOS GENERADOS:"
echo "  - .env: $APP_DIR/.env"
echo "  - Servicio: /etc/systemd/system/lfm.service"
echo ""
echo "CREDENCIALES (GUARDAR):"
echo "  - MySQL User: $DB_USER"
echo "  - MySQL Pass: $DB_PASS"
echo "  - JWT Secret: $JWT_SECRET"
echo ""
echo "SIGUIENTES PASOS:"
echo "  1. Subir app.jar y files/ a $APP_DIR"
echo "  2. Ejecutar: sudo systemctl start lfm"
echo "  3. Verificar: sudo systemctl status lfm"
echo "  4. Ver logs: sudo journalctl -u lfm -f"
echo ""
echo "URL: $FRONTEND_URL"
