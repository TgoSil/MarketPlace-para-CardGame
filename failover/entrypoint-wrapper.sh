#!/bin/bash
# ==============================================================
# entrypoint-wrapper.sh
# Executa o entrypoint original do Bitnami e, em seguida,
# injeta o failover_command correto no pgpool.conf gerado.
# ==============================================================
set -euo pipefail

CONF_FILE="/opt/bitnami/pgpool/conf/pgpool.conf"
FAILOVER_SCRIPT="/opt/bitnami/pgpool/scripts/failover.sh"
SETUP_SCRIPT="/opt/bitnami/scripts/pgpool/setup.sh"

echo "[wrapper] Executando setup do Bitnami para gerar pgpool.conf..."
bash "$SETUP_SCRIPT"

echo "[wrapper] Injetando failover_command no pgpool.conf..."
sed -i "s|^failover_command = .*|failover_command = 'bash $FAILOVER_SCRIPT %d %h %p %D %m %H %M %P %r %R'|" "$CONF_FILE"

echo "[wrapper] Verificando injecao:"
grep "failover_command" "$CONF_FILE"

echo "[wrapper] Iniciando Pgpool-II..."
exec bash /opt/bitnami/scripts/pgpool/run.sh
