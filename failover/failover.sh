#!/bin/bash
# ==============================================================
# failover.sh - Executado pelo Pgpool quando um backend falha
# ==============================================================
#
# Argumentos do Pgpool:
#   $1  = ID do no que falhou        (%d)  0=primary, 1=replica
#   $2  = Hostname do no que falhou  (%h)
#   $3  = Porta do no que falhou     (%p)
#   $4  = Data dir do no que falhou  (%D)
#   $5  = ID do novo primary         (%m)
#   $6  = Hostname do novo primary   (%H)  <- replica a ser promovida
#   $7  = ID do antigo primary       (%M)
#   $8  = Porta do antigo primary    (%P)
#   $9  = Porta do novo primary      (%r)
#   $10 = Data dir do novo primary   (%R)
#
# Estrategia de promocao:
#   Usa "pg_ctl promote" via docker exec dentro do container da
#   replica, que nao exige permissoes especiais de usuario SQL.
# ==============================================================

set -uo pipefail

FAILED_NODE_ID="${1:-}"
FAILED_NODE_HOST="${2:-}"
FAILED_NODE_PORT="${3:-5432}"
NEW_PRIMARY_ID="${5:-}"
NEW_PRIMARY_HOST="${6:-}"

LOG_DIR="/opt/bitnami/pgpool/logs"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/failover.log"

log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $*"
    echo "$msg"
    echo "$msg" >> "$LOG_FILE"
}

log "======================================================="
log "  EVENTO DE FAILOVER DETECTADO"
log "  No com falha  : ID=$FAILED_NODE_ID | $FAILED_NODE_HOST:$FAILED_NODE_PORT"
log "  Novo primary  : ID=$NEW_PRIMARY_ID | $NEW_PRIMARY_HOST"
log "======================================================="

# Se a replica caiu, nenhuma acao necessaria
if [ "$FAILED_NODE_ID" != "0" ]; then
    log "REPLICA caiu (no ID=$FAILED_NODE_ID). Nenhuma promocao necessaria."
    exit 0
fi

log "PRIMARY caiu! Promovendo replica: $NEW_PRIMARY_HOST"

# O nome do container Docker e igual ao hostname dentro da rede Docker.
# Usamos esse nome para chamar "pg_ctl promote" via docker exec.
REPLICA_CONTAINER="$NEW_PRIMARY_HOST"

# Localiza o data directory do PostgreSQL dentro do container
PG_DATA="/bitnami/postgresql/data"

MAX_TENTATIVAS=5
INTERVALO=3
SUCESSO=false

for i in $(seq 1 $MAX_TENTATIVAS); do
    log "Tentativa $i/$MAX_TENTATIVAS: pg_ctl promote em $REPLICA_CONTAINER..."

    RESULTADO=$(docker exec "$REPLICA_CONTAINER" \
        bash -c "pg_ctl promote -D $PG_DATA -W" 2>&1) || true

    if echo "$RESULTADO" | grep -qiE "server promoted|already|done"; then
        log "SUCESSO: $REPLICA_CONTAINER promovida a primary!"
        SUCESSO=true
        break
    else
        log "Tentativa $i falhou. Resposta: $RESULTADO"
        if [ "$i" -lt "$MAX_TENTATIVAS" ]; then
            log "Aguardando ${INTERVALO}s..."
            sleep "$INTERVALO"
        fi
    fi
done

if [ "$SUCESSO" != "true" ]; then
    log "ERRO CRITICO: Nao foi possivel promover $REPLICA_CONTAINER apos $MAX_TENTATIVAS tentativas."
    exit 1
fi

log "Failover concluido. Novo primary: $REPLICA_CONTAINER"
log "======================================================="
exit 0
